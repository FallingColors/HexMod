package at.petrak.hexcasting.api.casting.eval.vm

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.PatternShapeMatch
import at.petrak.hexcasting.api.casting.PatternShapeMatch.*
import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.eval.*
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.*
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.mod.HexTags
import at.petrak.hexcasting.api.utils.*
import at.petrak.hexcasting.common.casting.PatternRegistryManifest
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel

/**
 * The virtual machine! This is the glue that determines the next iteration of a [CastingImage], using a
 * [CastingEnvironment] to affect the world.
 */
class CastingVM(var image: CastingImage, val env: CastingEnvironment) {
    /**
     * Execute a single iota.
     */
    fun queueExecuteAndWrapIota(iota: Iota, world: ServerLevel): ExecutionClientView = queueExecuteAndWrapIotas(listOf(iota), world)

    /**
     * The main entrypoint to the VM. Given a list of iotas, execute them in sequence, and return whatever the client
     * needs to see.
     *
     * Mutates this
     */
    fun queueExecuteAndWrapIotas(iotas: List<Iota>, world: ServerLevel): ExecutionClientView {
        // Initialize the continuation stack to a single top-level eval for all iotas.
        var continuation = SpellContinuation.Done.pushFrame(FrameEvaluate(SpellList.LList(0, iotas), false))
        // Begin aggregating info
        val info = TempControllerInfo(earlyExit = false)
        var lastResolutionType = ResolvedPatternType.UNRESOLVED
        while (continuation is SpellContinuation.NotDone && !info.earlyExit) {
            // Take the top of the continuation stack...
            val next = continuation.frame
            // ...and execute it.
            // TODO there used to be error checking code here; I'm pretty sure any and all mishaps should already
            // get caught and folded into CastResult by evaluate.
            val image2 = next.evaluate(continuation.next, world, this)
            // Then write all pertinent data back to the harness for the next iteration.
            if (image2.newData != null) {
                this.image = image2.newData
            }
            this.env.postExecution(image2)

            continuation = image2.continuation
            lastResolutionType = image2.resolutionType
            performSideEffects(info, image2.sideEffects)
            info.earlyExit = info.earlyExit || !lastResolutionType.success
        }

        if (continuation is SpellContinuation.NotDone) {
            lastResolutionType =
                if (lastResolutionType.success) ResolvedPatternType.EVALUATED else ResolvedPatternType.ERRORED
        }

        val (stackDescs, ravenmind) = generateDescs()

        val isStackClear = image.stack.isEmpty() && image.parenCount == 0 && !image.escapeNext
        return ExecutionClientView(isStackClear, lastResolutionType, stackDescs, ravenmind)
    }

    /**
     * this DOES NOT THROW THINGS
     */
    @Throws()
    fun executeInner(iota: Iota, world: ServerLevel, continuation: SpellContinuation): CastResult {
        try {
            // TODO we can have a special intro/retro sound
            // ALSO TODO need to add reader macro-style things
            try {
                this.handleParentheses(iota)?.let { (data, resolutionType) ->
                    return@executeInner CastResult(continuation, data, listOf(), resolutionType, HexEvalSounds.ADD_PATTERN)
                }
            } catch (e: MishapTooManyCloseParens) {
                // This is ridiculous and needs to be fixed
                return CastResult(
                    continuation,
                    null,
                    listOf(
                        OperatorSideEffect.DoMishap(
                            e,
                            Mishap.Context(
                                (iota as? PatternIota)?.pattern ?: HexPattern(HexDir.WEST),
                                HexAPI.instance().getRawHookI18n(HexAPI.modLoc("close_paren"))
                            )
                        )
                    ),
                    ResolvedPatternType.ERRORED,
                    HexEvalSounds.MISHAP
                )
            }

            if (iota is PatternIota) {
                return executePattern(iota.pattern, world, continuation)
            } else {
                return CastResult(
                    continuation,
                    null,
                    listOf(
                        OperatorSideEffect.DoMishap(
                            MishapUnescapedValue(iota),
                            Mishap.Context(HexPattern(HexDir.WEST), null)
                        )
                    ), // Should never matter
                    ResolvedPatternType.INVALID,
                    HexEvalSounds.MISHAP
                )
            }
        } catch (exception: Exception) {
            // This means something very bad has happened
            exception.printStackTrace()
            return CastResult(
                continuation,
                null,
                listOf(
                    OperatorSideEffect.DoMishap(
                        MishapInternalException(exception),
                        Mishap.Context(
                            (iota as? PatternIota)?.pattern ?: HexPattern(HexDir.WEST),
                            null
                        )
                    )
                ),
                ResolvedPatternType.ERRORED,
                HexEvalSounds.MISHAP
            )
        }
    }

    /**
     * When the server gets a packet from the client with a new pattern,
     * handle it functionally.
     */
    private fun executePattern(newPat: HexPattern, world: ServerLevel, continuation: SpellContinuation): CastResult {
        var castedName: Component? = null
        try {
            val lookup = PatternRegistryManifest.matchPattern(newPat, world, false)
            this.env.precheckAction(lookup)

            val action = if (lookup is Normal || lookup is PerWorld) {
                val key = when (lookup) {
                    is Normal -> lookup.key
                    is PerWorld -> lookup.key
                    else -> throw IllegalStateException()
                }

                val reqsEnlightenment = isOfTag(IXplatAbstractions.INSTANCE.actionRegistry, key, HexTags.Actions.REQUIRES_ENLIGHTENMENT)

                castedName = HexAPI.instance().getActionI18n(key, reqsEnlightenment)

                IXplatAbstractions.INSTANCE.actionRegistry.get(key)!!.action
            } else if (lookup is Special) {
                castedName = lookup.handler.name
                lookup.handler.act()
            } else if (lookup is PatternShapeMatch.Nothing) {
                throw MishapInvalidPattern()
            } else throw IllegalStateException()

            val opCount = if (this.image.userData.contains(HexAPI.OP_COUNT_USERDATA)) {
                this.image.userData.getInt(HexAPI.OP_COUNT_USERDATA)
            } else {
                this.image.userData.putInt(HexAPI.OP_COUNT_USERDATA, 0)
                0
            }
            if (opCount + 1 > HexConfig.server().maxOpCount()) {
                throw MishapEvalTooMuch()
            }
            this.image.userData.putInt(HexAPI.OP_COUNT_USERDATA, opCount + 1)

            val sideEffects = mutableListOf<OperatorSideEffect>()
            var stack2: List<Iota>? = null
            var cont2 = continuation
            var userData2: CompoundTag? = null

            val result = action.operate(
                this.env,
                this.image.stack.toMutableList(),
                this.image.userData.copy(),
                continuation
            )
            cont2 = result.newContinuation
            stack2 = result.newStack
            userData2 = result.newUserdata
            // TODO parens also break prescience
            sideEffects.addAll(result.sideEffects)

            val hereFd = this.image
            val fd = if (stack2 != null) {
                hereFd.copy(
                    stack = stack2,
                    userData = userData2,
                )
            } else {
                hereFd
            }

            return CastResult(
                cont2,
                fd,
                sideEffects,
                ResolvedPatternType.EVALUATED,
                env.soundType,
            )

        } catch (mishap: Mishap) {
            return CastResult(
                continuation,
                null,
                listOf(OperatorSideEffect.DoMishap(mishap, Mishap.Context(newPat, castedName))),
                mishap.resolutionType(env),
                HexEvalSounds.MISHAP
            )
        }
    }

    /**
     * Execute the side effects of a pattern, updating our aggregated info.
     */
    fun performSideEffects(info: TempControllerInfo, sideEffects: List<OperatorSideEffect>) {
        for (haskellProgrammersShakingandCryingRN in sideEffects) {
            val mustStop = haskellProgrammersShakingandCryingRN.performEffect(this)
            if (mustStop) {
                info.earlyExit = true
                break
            }
        }
    }

    fun generateDescs(): Pair<List<CompoundTag>, CompoundTag?> {
        val stackDescs = this.image.stack.map { IotaType.serialize(it) }
        val ravenmind = if (this.image.userData.contains(HexAPI.RAVENMIND_USERDATA)) {
            this.image.userData.getCompound(HexAPI.RAVENMIND_USERDATA)
        } else null
        return Pair(stackDescs, ravenmind)
    }

    /**
     * Return a non-null value if we handled this in some sort of parenthesey way,
     * either escaping it onto the stack or changing the parenthese-handling state.
     */
    @Throws(MishapTooManyCloseParens::class)
    private fun handleParentheses(iota: Iota): Pair<CastingImage, ResolvedPatternType>? {
        val sig = (iota as? PatternIota)?.pattern?.anglesSignature()

        var displayDepth = this.image.parenCount

        val out = if (displayDepth > 0) {
            if (this.image.escapeNext) {
                val newParens = this.image.parenthesized.toMutableList()
                newParens.add(iota)
                this.image.copy(
                    escapeNext = false,
                    parenthesized = newParens
                ) to ResolvedPatternType.ESCAPED
            } else {

                when (sig) {
                    SpecialPatterns.CONSIDERATION.anglesSignature() -> {
                        this.image.copy(
                            escapeNext = true,
                        ) to ResolvedPatternType.EVALUATED
                    }

                    SpecialPatterns.INTROSPECTION.anglesSignature() -> {
                        // we have escaped the parens onto the stack; we just also record our count.
                        val newParens = this.image.parenthesized.toMutableList()
                        newParens.add(iota)
                        this.image.copy(
                            parenthesized = newParens,
                            parenCount = this.image.parenCount + 1
                        ) to if (this.image.parenCount == 0) ResolvedPatternType.EVALUATED else ResolvedPatternType.ESCAPED
                    }

                    SpecialPatterns.RETROSPECTION.anglesSignature() -> {
                        val newParenCount = this.image.parenCount - 1
                        displayDepth--
                        if (newParenCount == 0) {
                            val newStack = this.image.stack.toMutableList()
                            newStack.add(ListIota(this.image.parenthesized.toList()))
                            this.image.copy(
                                stack = newStack,
                                parenCount = newParenCount,
                                parenthesized = listOf()
                            ) to ResolvedPatternType.EVALUATED
                        } else if (newParenCount < 0) {
                            throw MishapTooManyCloseParens()
                        } else {
                            // we have this situation: "(()"
                            // we need to add the close paren
                            val newParens = this.image.parenthesized.toMutableList()
                            newParens.add(iota)
                            this.image.copy(
                                parenCount = newParenCount,
                                parenthesized = newParens
                            ) to ResolvedPatternType.ESCAPED
                        }
                    }

                    else -> {
                        val newParens = this.image.parenthesized.toMutableList()
                        newParens.add(iota)
                        this.image.copy(
                            parenthesized = newParens
                        ) to ResolvedPatternType.ESCAPED
                    }
                }
            }
        } else if (this.image.escapeNext) {
            val newStack = this.image.stack.toMutableList()
            newStack.add(iota)
            this.image.copy(
                stack = newStack,
                escapeNext = false,
            ) to ResolvedPatternType.ESCAPED
        } else {
            when (sig) {
                SpecialPatterns.CONSIDERATION.anglesSignature() -> {
                    this.image.copy(
                        escapeNext = true
                    ) to ResolvedPatternType.EVALUATED
                }

                SpecialPatterns.INTROSPECTION.anglesSignature() -> {
                    this.image.copy(
                        parenCount = this.image.parenCount + 1
                    ) to ResolvedPatternType.EVALUATED
                }

                SpecialPatterns.RETROSPECTION.anglesSignature() -> {
                    throw MishapTooManyCloseParens()
                }

                else -> {
                    null
                }
            }
        }

        // TODO: replace this once we can read things from the client
        /*
        if (out != null) {
            val display = if (iota is PatternIota) {
                PatternNameHelper.representationForPattern(iota.pattern)
                    .copy()
                    .withStyle(if (out.second == ResolvedPatternType.ESCAPED) ChatFormatting.YELLOW else ChatFormatting.AQUA)
            } else iota.display()
            displayPatternDebug(this.escapeNext, displayDepth, display)
        }
        */
        return out
    }

    data class TempControllerInfo(
        var earlyExit: Boolean,
    )

    companion object {
        @JvmStatic
        fun empty(env: CastingEnvironment): CastingVM {
            return CastingVM(CastingImage(), env)
        }
    }
}
