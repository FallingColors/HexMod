package at.petrak.hexcasting.api.casting.eval

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.PatternShapeMatch
import at.petrak.hexcasting.api.casting.PatternShapeMatch.*
import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate
import at.petrak.hexcasting.api.casting.eval.vm.FunctionalData
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.*
import at.petrak.hexcasting.api.misc.DiscoveryHandlers
import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.mod.HexTags
import at.petrak.hexcasting.api.utils.*
import at.petrak.hexcasting.common.casting.PatternRegistryManifest
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import at.petrak.hexcasting.xplat.IXplatAbstractions
import com.mojang.datafixers.util.Either
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.phys.Vec3

/**
 * Keeps track of a player casting a spell on the server.
 * It's stored as NBT on the player.
 *
 * TODO oh god this entire class needs a gigantic refactor. why are there like 6 different entrypoints for casting
 * a pattern. oh god.
 */
class CastingHarness private constructor(
    var stack: MutableList<Iota>,
    var ravenmind: Iota?,
    var parenCount: Int,
    var parenthesized: List<Iota>,
    var escapeNext: Boolean,
    val ctx: CastingEnvironment,
    val prepackagedColorizer: FrozenColorizer? // for trinkets with colorizers
) {

    @JvmOverloads
    constructor(
        ctx: CastingEnvironment,
        prepackagedColorizer: FrozenColorizer? = null
    ) : this(mutableListOf(), null, 0, mutableListOf(), false, ctx, prepackagedColorizer)

    /**
     * Execute a single iota.
     */
    fun executeIota(iota: Iota, world: ServerLevel): ControllerInfo = executeIotas(listOf(iota), world)

    private fun displayPatternDebug(escapeNext: Boolean, parenCount: Int, iotaRepresentation: Component) {
        if (this.ctx.debugPatterns) {
            val display = "  ".repeat(parenCount).asTextComponent
            if (escapeNext)
                display.append("\\ ".asTextComponent.gold)
            display.append(iotaRepresentation)

            this.ctx.caster.sendSystemMessage(display)
        }
    }

    /**
     * Given a list of iotas, execute them in sequence.
     */
    fun executeIotas(iotas: List<Iota>, world: ServerLevel): ControllerInfo {
        // Initialize the continuation stack to a single top-level eval for all iotas.
        var continuation = SpellContinuation.Done.pushFrame(FrameEvaluate(SpellList.LList(0, iotas), false))
        // Begin aggregating info
        val info = TempControllerInfo(earlyExit = false)
        var lastResolutionType = ResolvedPatternType.UNRESOLVED
        var sound = HexEvalSounds.NOTHING
        while (continuation is SpellContinuation.NotDone && !info.earlyExit) {
            // Take the top of the continuation stack...
            val next = continuation.frame
            // ...and execute it.
            // TODO there used to be error checking code here; I'm pretty sure any and all mishaps should already
            // get caught and folded into CastResult by evaluate.
            val result = next.evaluate(continuation.next, world, this)
            // Then write all pertinent data back to the harness for the next iteration.
            if (result.newData != null) {
                this.applyFunctionalData(result.newData)
            }
            continuation = result.continuation
            lastResolutionType = result.resolutionType
            performSideEffects(info, result.sideEffects)
            info.earlyExit = info.earlyExit || !lastResolutionType.success
            sound = if (result.sound == HexEvalSounds.MISHAP) {
                HexEvalSounds.MISHAP
            } else {
                sound.greaterOf(result.sound)
            }
        }

        sound.sound?.let {
            this.ctx.world.playSound(
                null, this.ctx.position.x, this.ctx.position.y, this.ctx.position.z, it,
                SoundSource.PLAYERS, 1f, 1f
            )
            // TODO: is it worth mixing in to the immut map and making our own game event with blackjack and hookers
            this.ctx.world.gameEvent(this.ctx.caster, GameEvent.ITEM_INTERACT_FINISH, this.ctx.position)
        }

        if (continuation is SpellContinuation.NotDone) {
            lastResolutionType =
                if (lastResolutionType.success) ResolvedPatternType.EVALUATED else ResolvedPatternType.ERRORED
        }

        val (stackDescs, parenDescs, ravenmind) = generateDescs()

        return ControllerInfo(
            this.stack.isEmpty() && this.parenCount == 0 && !this.escapeNext,
            lastResolutionType,
            stackDescs,
            parenDescs,
            ravenmind,
            this.parenCount
        )
    }

    /**
     * this DOES NOT THROW THINGS
     */
    @Throws()
    fun getUpdate(iota: Iota, world: ServerLevel, continuation: SpellContinuation): CastResult {
        try {
            // TODO we can have a special intro/retro sound
            // ALSO TODO need to add reader macro-style things
            try {
                this.handleParentheses(iota)?.let { (data, resolutionType) ->
                    return@getUpdate CastResult(continuation, data, listOf(), resolutionType, HexEvalSounds.OPERATOR)
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
                return updateWithPattern(iota.pattern, world, continuation)
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
                        MishapError(exception),
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
    private fun updateWithPattern(newPat: HexPattern, world: ServerLevel, continuation: SpellContinuation): CastResult {
        var castedName: Component? = null
        try {
            val lookup = PatternRegistryManifest.matchPattern(newPat, world, false)
            val lookupResult: Either<Action, List<OperatorSideEffect>> = if (lookup is Normal || lookup is PerWorld) {
                val key = when (lookup) {
                    is Normal -> lookup.key
                    is PerWorld -> lookup.key
                    else -> throw IllegalStateException()
                }

                val reqsEnlightenment = isOfTag(IXplatAbstractions.INSTANCE.actionRegistry, key, HexTags.Actions.REQUIRES_ENLIGHTENMENT)
                val canEnlighten = isOfTag(IXplatAbstractions.INSTANCE.actionRegistry, key, HexTags.Actions.CAN_START_ENLIGHTEN)

                castedName = HexAPI.instance().getActionI18n(key, reqsEnlightenment)

                if (!ctx.isCasterEnlightened && reqsEnlightenment) {
                    Either.right(listOf(OperatorSideEffect.RequiredEnlightenment(canEnlighten)))
                } else {
                    val regiEntry = IXplatAbstractions.INSTANCE.actionRegistry.get(key)!!
                    Either.left(regiEntry.action)
                }
            } else if (lookup is Special) {
                castedName = lookup.handler.name
                Either.left(lookup.handler.act())
            } else if (lookup is PatternShapeMatch.Nothing) {
                throw MishapInvalidPattern()
            } else {
                throw IllegalStateException()
            }
            // TODO: the config denylist should be handled per VM type.
            // I just removed it for now, should re-add it...

            val sideEffects = mutableListOf<OperatorSideEffect>()
            var stack2: List<Iota>? = null
            var cont2 = continuation
            var ravenmind2: Iota? = null

            if (lookupResult.left().isPresent) {
                val action = lookupResult.left().get()
                displayPatternDebug(false, 0, castedName)
                val result = action.operate(
                    continuation,
                    this.stack.toMutableList(),
                    this.ravenmind,
                    this.ctx
                )
                cont2 = result.newContinuation
                stack2 = result.newStack
                ravenmind2 = result.newRavenmind
                // TODO parens also break prescience
                sideEffects.addAll(result.sideEffects)
            } else {
                val problems = lookupResult.right().get()
                sideEffects.addAll(problems)
            }

            // Stick a poofy particle effect at the caster position
            // TODO again this should be on the VM lalala
            if (this.ctx.spellCircle == null)
                sideEffects.add(
                    OperatorSideEffect.Particles(
                        ParticleSpray(
                            this.ctx.position,
                            Vec3(0.0, 1.0, 0.0),
                            0.5, 1.0
                        )
                    )
                )

            val hereFd = this.getFunctionalData()
            val fd = if (stack2 != null) {
                hereFd.copy(
                    stack = stack2,
                    ravenmind = ravenmind2
                )
            } else {
                hereFd
            }

            // TODO again this should be per VM
            var soundType = if (this.ctx.source == CastingEnvironment.CastSource.STAFF) {
                HexEvalSounds.OPERATOR
            } else {
                HexEvalSounds.NOTHING
            }
            for (se in sideEffects) {
                if (se is OperatorSideEffect.AttemptSpell) {
                    soundType = if (se.hasCastingSound) {
                        soundType.greaterOf(HexEvalSounds.SPELL)
                    } else {
                        // WITH CATLIKE TREAD
                        // UPON OUR PREY WE STEAL
                        HexEvalSounds.NOTHING
                    }
                } else if (se is OperatorSideEffect.DoMishap) {
                    soundType = HexEvalSounds.MISHAP
                }
            }
            return CastResult(
                cont2,
                fd,
                sideEffects,
                ResolvedPatternType.EVALUATED,
                soundType,
            )

        } catch (mishap: Mishap) {
            return CastResult(
                continuation,
                null,
                listOf(OperatorSideEffect.DoMishap(mishap, Mishap.Context(newPat, castedName))),
                mishap.resolutionType(ctx),
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

    fun generateDescs() = Triple(
        stack.map(HexIotaTypes::serialize),
        parenthesized.map(HexIotaTypes::serialize),
        ravenmind?.let(HexIotaTypes::serialize)
    )

    /**
     * Return the functional update represented by the current state (for use with `copy`)
     */
    fun getFunctionalData() = FunctionalData(
        this.stack.toList(),
        this.parenCount,
        this.parenthesized.toList(),
        this.escapeNext,
        this.ravenmind,
    )

    /**
     * Apply the functional update.
     */
    fun applyFunctionalData(data: FunctionalData) {
        this.stack.clear()
        this.stack.addAll(data.stack)
        this.parenCount = data.parenCount
        this.parenthesized = data.parenthesized
        this.escapeNext = data.escapeNext
        this.ravenmind = data.ravenmind
    }

    /**
     * Return a non-null value if we handled this in some sort of parenthesey way,
     * either escaping it onto the stack or changing the parenthese-handling state.
     */
    @Throws(MishapTooManyCloseParens::class)
    private fun handleParentheses(iota: Iota): Pair<FunctionalData, ResolvedPatternType>? {
        val sig = (iota as? PatternIota)?.pattern?.anglesSignature()

        var displayDepth = this.parenCount

        val out = if (this.parenCount > 0) {
            if (this.escapeNext) {
                val newParens = this.parenthesized.toMutableList()
                newParens.add(iota)
                this.getFunctionalData().copy(
                    escapeNext = false,
                    parenthesized = newParens
                ) to ResolvedPatternType.ESCAPED
            } else {

                when (sig) {
                    SpecialPatterns.CONSIDERATION.anglesSignature() -> {
                        this.getFunctionalData().copy(
                            escapeNext = true,
                        ) to ResolvedPatternType.EVALUATED
                    }

                    SpecialPatterns.INTROSPECTION.anglesSignature() -> {
                        // we have escaped the parens onto the stack; we just also record our count.
                        val newParens = this.parenthesized.toMutableList()
                        newParens.add(iota)
                        this.getFunctionalData().copy(
                            parenthesized = newParens,
                            parenCount = this.parenCount + 1
                        ) to if (this.parenCount == 0) ResolvedPatternType.EVALUATED else ResolvedPatternType.ESCAPED
                    }

                    SpecialPatterns.RETROSPECTION.anglesSignature() -> {
                        val newParenCount = this.parenCount - 1
                        displayDepth--
                        if (newParenCount == 0) {
                            val newStack = this.stack.toMutableList()
                            newStack.add(ListIota(this.parenthesized.toList()))
                            this.getFunctionalData().copy(
                                stack = newStack,
                                parenCount = newParenCount,
                                parenthesized = listOf()
                            ) to ResolvedPatternType.EVALUATED
                        } else if (newParenCount < 0) {
                            throw MishapTooManyCloseParens()
                        } else {
                            // we have this situation: "(()"
                            // we need to add the close paren
                            val newParens = this.parenthesized.toMutableList()
                            newParens.add(iota)
                            this.getFunctionalData().copy(
                                parenCount = newParenCount,
                                parenthesized = newParens
                            ) to ResolvedPatternType.ESCAPED
                        }
                    }

                    else -> {
                        val newParens = this.parenthesized.toMutableList()
                        newParens.add(iota)
                        this.getFunctionalData().copy(
                            parenthesized = newParens
                        ) to ResolvedPatternType.ESCAPED
                    }
                }
            }
        } else if (this.escapeNext) {
            val newStack = this.stack.toMutableList()
            newStack.add(iota)
            this.getFunctionalData().copy(
                stack = newStack,
                escapeNext = false,
            ) to ResolvedPatternType.ESCAPED
        } else {
            when (sig) {
                SpecialPatterns.CONSIDERATION.anglesSignature() -> {
                    this.getFunctionalData().copy(
                        escapeNext = true
                    ) to ResolvedPatternType.EVALUATED
                }

                SpecialPatterns.INTROSPECTION.anglesSignature() -> {
                    this.getFunctionalData().copy(
                        parenCount = this.parenCount + 1
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

    fun getColorizer(): FrozenColorizer {
        if (this.prepackagedColorizer != null)
            return this.prepackagedColorizer

        return IXplatAbstractions.INSTANCE.getColorizer(this.ctx.caster)
    }


    fun serializeToNBT() = NBTBuilder {
        TAG_STACK %= stack.serializeToNBT()

        if (ravenmind != null)
            TAG_LOCAL %= HexIotaTypes.serialize(ravenmind!!)
        TAG_PAREN_COUNT %= parenCount
        TAG_ESCAPE_NEXT %= escapeNext

        TAG_PARENTHESIZED %= parenthesized.serializeToNBT()

        if (prepackagedColorizer != null)
            TAG_PREPACKAGED_COLORIZER %= prepackagedColorizer.serializeToNBT()
    }


    companion object {
        const val TAG_STACK = "stack"
        const val TAG_LOCAL = "local"
        const val TAG_PAREN_COUNT = "open_parens"
        const val TAG_PARENTHESIZED = "parenthesized"
        const val TAG_ESCAPE_NEXT = "escape_next"
        const val TAG_PREPACKAGED_COLORIZER = "prepackaged_colorizer"

        init {
            DiscoveryHandlers.addMediaHolderDiscoverer {
                it.ctx.caster.inventory.items
                    .filter(::isMediaItem)
                    .mapNotNull(IXplatAbstractions.INSTANCE::findMediaHolder)
            }
            DiscoveryHandlers.addMediaHolderDiscoverer {
                it.ctx.caster.inventory.armor
                    .filter(::isMediaItem)
                    .mapNotNull(IXplatAbstractions.INSTANCE::findMediaHolder)
            }
            DiscoveryHandlers.addMediaHolderDiscoverer {
                it.ctx.caster.inventory.offhand
                    .filter(::isMediaItem)
                    .mapNotNull(IXplatAbstractions.INSTANCE::findMediaHolder)
            }
        }

        @JvmStatic
        fun fromNBT(nbt: CompoundTag, ctx: CastingEnvironment): CastingHarness {
            return try {
                val stack = mutableListOf<Iota>()
                val stackTag = nbt.getList(TAG_STACK, Tag.TAG_COMPOUND)
                for (subtag in stackTag) {
                    val datum = HexIotaTypes.deserialize(subtag.asCompound, ctx.world)
                    stack.add(datum)
                }

                val ravenmind = if (nbt.contains(TAG_LOCAL))
                    HexIotaTypes.deserialize(nbt.getCompound(TAG_LOCAL), ctx.world)
                else
                    null

                val parenthesized = mutableListOf<Iota>()
                val parenTag = nbt.getList(TAG_PARENTHESIZED, Tag.TAG_COMPOUND)
                for (subtag in parenTag) {
                    parenthesized.add(HexIotaTypes.deserialize(subtag.downcast(CompoundTag.TYPE), ctx.world))
                }

                val parenCount = nbt.getInt(TAG_PAREN_COUNT)
                val escapeNext = nbt.getBoolean(TAG_ESCAPE_NEXT)

                val colorizer = if (nbt.contains(TAG_PREPACKAGED_COLORIZER)) {
                    FrozenColorizer.fromNBT(nbt.getCompound(TAG_PREPACKAGED_COLORIZER))
                } else {
                    null
                }

                CastingHarness(stack, ravenmind, parenCount, parenthesized, escapeNext, ctx, colorizer)
            } catch (exn: Exception) {
                CastingHarness(ctx)
            }
        }
    }

    data class TempControllerInfo(
        var earlyExit: Boolean,
    )
}
