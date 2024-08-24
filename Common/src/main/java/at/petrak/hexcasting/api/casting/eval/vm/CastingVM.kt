package at.petrak.hexcasting.api.casting.eval.vm

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.PatternShapeMatch.*
import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.eval.*
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage.ParenthesizedIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.*
import at.petrak.hexcasting.api.utils.*
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel

/**
 * The virtual machine! This is the glue that determines the next iteration of a [CastingImage], using a
 * [CastingEnvironment] to affect the world.
 */
class CastingVM(var image: CastingImage, val env: CastingEnvironment) {
    init {
        env.triggerCreateEvent(image.userData)
    }

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
            val image2 = next.evaluate(continuation.next, world, this).let { result ->
                // if stack is unable to be serialized, have the result be an error
                if (result.newData != null && IotaType.isTooLargeToSerialize(result.newData.stack)) {
                    result.copy(
                        newData = null,
                        sideEffects = listOf(OperatorSideEffect.DoMishap(MishapStackSize(), Mishap.Context(null, null))),
                        resolutionType = ResolvedPatternType.ERRORED,
                        sound = HexEvalSounds.MISHAP,
                    )
                } else {
                    result
                }
            }

            // Then write all pertinent data back to the harness for the next iteration.
            if (image2.newData != null) {
                this.image = image2.newData
            }
            this.env.postExecution(image2)

            continuation = image2.continuation
            lastResolutionType = image2.resolutionType
            try {
                performSideEffects(info, image2.sideEffects)
            } catch (e: Exception) {
                e.printStackTrace()
                performSideEffects(info, listOf(OperatorSideEffect.DoMishap(MishapInternalException(e), Mishap.Context(null, null))))
            }
            info.earlyExit = info.earlyExit || !lastResolutionType.success
        }

        if (continuation is SpellContinuation.NotDone) {
            lastResolutionType =
                if (lastResolutionType.success) ResolvedPatternType.EVALUATED else ResolvedPatternType.ERRORED
        }

        val (stackDescs, ravenmind) = generateDescs()

        val isStackClear = image.stack.isEmpty() && image.parenCount == 0 && !image.escapeNext && ravenmind == null

        this.env.postCast(image)
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
                    return@executeInner CastResult(iota, continuation, data, listOf(), resolutionType, HexEvalSounds.NORMAL_EXECUTE)
                }
            } catch (e: MishapTooManyCloseParens) {
                // This is ridiculous and needs to be fixed
                return CastResult(
                    iota,
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

            return iota.execute(this, world, continuation)
        } catch (exception: Exception) {
            // This means something very bad has happened
            exception.printStackTrace()
            return CastResult(
                iota,
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
        val sig = (iota as? PatternIota)?.pattern?.angles

        var displayDepth = this.image.parenCount

        val out = if (displayDepth > 0) {
            if (this.image.escapeNext) {
                val newParens = this.image.parenthesized.toMutableList()
                newParens.add(ParenthesizedIota(iota, true))
                this.image.copy(
                    escapeNext = false,
                    parenthesized = newParens
                ) to ResolvedPatternType.ESCAPED
            } else {

                when (sig) {
                    SpecialPatterns.CONSIDERATION.angles -> {
                        this.image.copy(
                            escapeNext = true,
                        ) to ResolvedPatternType.EVALUATED
                    }

                    SpecialPatterns.EVANITION.angles -> {
                        val newParens = this.image.parenthesized.toMutableList()
                        val last = newParens.removeLastOrNull()
                        val newParenCount = this.image.parenCount + if (last == null || last.escaped || last.iota !is PatternIota) 0 else when (last.iota.pattern) {
                            SpecialPatterns.INTROSPECTION -> -1
                            SpecialPatterns.RETROSPECTION -> 1
                            else -> 0
                        }
                        this.image.copy(parenthesized = newParens, parenCount = newParenCount) to if (last == null) ResolvedPatternType.ERRORED else ResolvedPatternType.UNDONE
                    }

                    SpecialPatterns.INTROSPECTION.angles -> {
                        // we have escaped the parens onto the stack; we just also record our count.
                        val newParens = this.image.parenthesized.toMutableList()
                        newParens.add(ParenthesizedIota(iota, false))
                        this.image.copy(
                            parenthesized = newParens,
                            parenCount = this.image.parenCount + 1
                        ) to if (this.image.parenCount == 0) ResolvedPatternType.EVALUATED else ResolvedPatternType.ESCAPED
                    }

                    SpecialPatterns.RETROSPECTION.angles -> {
                        val newParenCount = this.image.parenCount - 1
                        displayDepth--
                        if (newParenCount == 0) {
                            val newStack = this.image.stack.toMutableList()
                            newStack.add(ListIota(this.image.parenthesized.toList().map { it.iota }))
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
                            newParens.add(ParenthesizedIota(iota, false))
                            this.image.copy(
                                parenCount = newParenCount,
                                parenthesized = newParens
                            ) to ResolvedPatternType.ESCAPED
                        }
                    }

                    else -> {
                        val newParens = this.image.parenthesized.toMutableList()
                        newParens.add(ParenthesizedIota(iota, false))
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
                SpecialPatterns.CONSIDERATION.angles -> {
                    this.image.copy(
                        escapeNext = true
                    ) to ResolvedPatternType.EVALUATED
                }

                SpecialPatterns.INTROSPECTION.angles -> {
                    this.image.copy(
                        parenCount = this.image.parenCount + 1
                    ) to ResolvedPatternType.EVALUATED
                }

                SpecialPatterns.RETROSPECTION.angles -> {
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
