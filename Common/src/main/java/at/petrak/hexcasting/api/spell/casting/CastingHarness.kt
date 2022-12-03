package at.petrak.hexcasting.api.spell.casting

import at.petrak.hexcasting.api.PatternRegistry
import at.petrak.hexcasting.api.advancements.HexAdvancementTriggers
import at.petrak.hexcasting.api.block.circle.BlockEntityAbstractImpetus
import at.petrak.hexcasting.api.misc.DiscoveryHandlers
import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.misc.HexDamageSources
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.mod.HexStatistics
import at.petrak.hexcasting.api.mod.HexTags
import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.SpellList
import at.petrak.hexcasting.api.spell.casting.eval.ContinuationFrame
import at.petrak.hexcasting.api.spell.casting.eval.FrameEvaluate
import at.petrak.hexcasting.api.spell.casting.eval.FunctionalData
import at.petrak.hexcasting.api.spell.casting.eval.SpellContinuation
import at.petrak.hexcasting.api.spell.casting.sideeffects.EvalSound
import at.petrak.hexcasting.api.spell.casting.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.ListIota
import at.petrak.hexcasting.api.spell.iota.PatternIota
import at.petrak.hexcasting.api.spell.math.HexDir
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.spell.mishaps.*
import at.petrak.hexcasting.api.utils.*
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.ChatFormatting
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.util.Mth
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.phys.Vec3
import kotlin.math.max
import kotlin.math.min

/**
 * Keeps track of a player casting a spell on the server.
 * It's stored as NBT on the wand.
 */
class CastingHarness private constructor(
    var stack: MutableList<Iota>,
    var ravenmind: Iota?,
    var parenCount: Int,
    var parenthesized: List<Iota>,
    var escapeNext: Boolean,
    val ctx: CastingContext,
    val prepackagedColorizer: FrozenColorizer? // for trinkets with colorizers
) {

    @JvmOverloads
    constructor(
        ctx: CastingContext,
        prepackagedColorizer: FrozenColorizer? = null
    ) : this(mutableListOf(), null, 0, mutableListOf(), false, ctx, prepackagedColorizer)

    /**
     * Execute a single iota.
     */
    fun executeIota(iota: Iota, world: ServerLevel): ControllerInfo = executeIotas(listOf(iota), world)

    private fun displayPattern(escapeNext: Boolean, parenCount: Int, iotaRepresentation: Component) {
        if (this.ctx.debugPatterns) {
            val display = "  ".repeat(parenCount).asTextComponent
            if (escapeNext)
                display.append("\\ ".asTextComponent.gold)
            display.append(iotaRepresentation)

            this.ctx.caster.sendSystemMessage(display)
        }
    }

    private fun getOperatorForPattern(iota: Iota, world: ServerLevel): Action? {
        if (iota is PatternIota)
            return PatternRegistry.matchPattern(iota.pattern, world)
        return null
    }

    private fun getPatternForFrame(frame: ContinuationFrame): HexPattern? {
        if (frame !is FrameEvaluate) return null

        return (frame.list.car as? PatternIota)?.pattern
    }

    private fun getOperatorForFrame(frame: ContinuationFrame, world: ServerLevel): Action? {
        if (frame !is FrameEvaluate) return null

        return getOperatorForPattern(frame.list.car, world)
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
            val result = try {
                next.evaluate(continuation.next, world, this)
            } catch (mishap: Mishap) {
                val pattern = getPatternForFrame(next)
                val operator = try {
                    getOperatorForFrame(next, world)
                } catch (e: Throwable) {
                     null
                }
                CastResult(
                    continuation,
                    null,
                    mishap.resolutionType(ctx),
                    listOf(
                        OperatorSideEffect.DoMishap(
                            mishap,
                            Mishap.Context(pattern ?: HexPattern(HexDir.WEST), operator)
                        )
                    ),
                    HexEvalSounds.MISHAP,
                )
            }
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

    fun getUpdate(iota: Iota, world: ServerLevel, continuation: SpellContinuation): CastResult {
        try {
            // TODO we can have a special intro/retro sound
            this.handleParentheses(iota)?.let { (data, resolutionType) ->
                return@getUpdate CastResult(continuation, data, resolutionType, listOf(), HexEvalSounds.OPERATOR)
            }

            if (iota is PatternIota) {
                return updateWithPattern(iota.pattern, world, continuation)
            } else {
                return CastResult(
                    continuation,
                    null,
                    ResolvedPatternType.INVALID, // Should never matter
                    listOf(
                        OperatorSideEffect.DoMishap(
                            MishapUnescapedValue(iota),
                            Mishap.Context(HexPattern(HexDir.WEST), null)
                        )
                    ),
                    HexEvalSounds.MISHAP
                )
            }
        } catch (mishap: Mishap) {
            val operator = try {
                getOperatorForPattern(iota, world)
            } catch (e: Throwable) {
                null
            }
            return CastResult(
                continuation,
                null,
                mishap.resolutionType(ctx),
                listOf(
                    OperatorSideEffect.DoMishap(
                        mishap,
                        Mishap.Context(
                            (iota as? PatternIota)?.pattern ?: HexPattern(HexDir.WEST),
                            operator
                        )
                    )
                ),
                HexEvalSounds.MISHAP
            )
        } catch (exception: Exception) {
            // This means something very bad has happened
            exception.printStackTrace()
            val operator = try {
                getOperatorForPattern(iota, world)
            } catch (e: Throwable) {
                null
            }
            return CastResult(
                continuation,
                null,
                ResolvedPatternType.ERRORED,
                listOf(
                    OperatorSideEffect.DoMishap(
                        MishapError(exception),
                        Mishap.Context(
                            (iota as? PatternIota)?.pattern ?: HexPattern(HexDir.WEST),
                            operator
                        )
                    )
                ),
                HexEvalSounds.MISHAP
            )
        }
    }

    /**
     * When the server gets a packet from the client with a new pattern,
     * handle it functionally.
     */
    fun updateWithPattern(newPat: HexPattern, world: ServerLevel, continuation: SpellContinuation): CastResult {
        var actionIdPair: Pair<Action, ResourceLocation>? = null
        try {
            // Don't catch this one
            val mojangPair = PatternRegistry.matchPatternAndID(newPat, world)
            actionIdPair = mojangPair.first to mojangPair.second

            if (this.ctx.spellCircle == null && !HexConfig.server().isActionAllowed(actionIdPair.second)) {
                throw MishapDisallowedSpell()
            } else if (this.ctx.spellCircle != null
                && !HexConfig.server().isActionAllowedInCircles(actionIdPair.second)
            ) {
                throw MishapDisallowedSpell("disallowed_circle")
            }

            val pattern = actionIdPair.first

            val unenlightened = pattern.isGreat && !ctx.isCasterEnlightened

            val sideEffects = mutableListOf<OperatorSideEffect>()
            var stack2: List<Iota>? = null
            var cont2 = continuation
            var ravenmind2: Iota? = null

            if (!unenlightened || pattern.alwaysProcessGreatSpell) {
                displayPattern(false, 0, pattern.displayName)
                val result = pattern.operate(
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
            }

            if (unenlightened) {
                sideEffects.add(OperatorSideEffect.RequiredEnlightenment(pattern.causesBlindDiversion))
            }

            // Stick a poofy particle effect at the caster position
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

            var soundType = if (this.ctx.source == CastingContext.CastSource.STAFF) {
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
                ResolvedPatternType.EVALUATED,
                sideEffects,
                soundType,
            )

        } catch (mishap: Mishap) {
            return CastResult(
                continuation,
                null,
                mishap.resolutionType(ctx),
                listOf(OperatorSideEffect.DoMishap(mishap, Mishap.Context(newPat, actionIdPair?.first))),
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

        if (out != null) {
            val display = if (iota is PatternIota) {
                PatternNameHelper.representationForPattern(iota.pattern)
                    .copy()
                    .withStyle(if (out.second == ResolvedPatternType.ESCAPED) ChatFormatting.YELLOW else ChatFormatting.AQUA)
            } else iota.display()
            displayPattern(this.escapeNext, displayDepth, display)
        }
        return out
    }

    /**
     * Might cast from hitpoints.
     * Returns the media cost still remaining after we deplete everything. It will be <= 0 if we could pay for it.
     *
     * Also awards stats and achievements and such
     */
    fun withdrawMedia(mediaCost: Int, allowOvercast: Boolean): Int {
        // prevent poor impls from gaining you media
        if (mediaCost <= 0) return 0
        var costLeft = mediaCost

        val fake = this.ctx.caster.isCreative

        if (this.ctx.spellCircle != null) {
            if (fake)
                return 0

            val tile = this.ctx.world.getBlockEntity(this.ctx.spellCircle.impetusPos)
            if (tile is BlockEntityAbstractImpetus) {
                val mediaAvailable = tile.media
                if (mediaAvailable < 0)
                    return 0

                val mediaToTake = min(costLeft, mediaAvailable)
                costLeft -= mediaToTake
                tile.media = mediaAvailable - mediaToTake
            }
        } else {
            val casterStack = this.ctx.caster.getItemInHand(this.ctx.castingHand)
            val casterMediaHolder = IXplatAbstractions.INSTANCE.findMediaHolder(casterStack)
            val casterHexHolder = IXplatAbstractions.INSTANCE.findHexHolder(casterStack)
            val hexHolderDrawsFromInventory = if (casterHexHolder != null) {
                if (casterMediaHolder != null) {
                    val mediaAvailable = casterMediaHolder.withdrawMedia(-1, true)
                    val mediaToTake = min(costLeft, mediaAvailable)
                    if (!fake) casterMediaHolder.withdrawMedia(mediaToTake, false)
                    costLeft -= mediaToTake
                }
                casterHexHolder.canDrawMediaFromInventory()
            } else {
                false
            }

            if (casterStack.`is`(HexTags.Items.STAVES) || hexHolderDrawsFromInventory) {
                val mediaSources = DiscoveryHandlers.collectMediaHolders(this)
                    .sortedWith(Comparator(::compareMediaItem).reversed())
                for (source in mediaSources) {
                    costLeft -= extractMedia(source, costLeft, simulate = fake)
                    if (costLeft <= 0)
                        break
                }

                if (allowOvercast && costLeft > 0) {
                    // Cast from HP!
                    val mediaToHealth = HexConfig.common().mediaToHealthRate()
                    val healthToRemove = max(costLeft.toDouble() / mediaToHealth, 0.5)
                    val mediaAbleToCastFromHP = this.ctx.caster.health * mediaToHealth

                    val mediaToActuallyPayFor = min(mediaAbleToCastFromHP.toInt(), costLeft)
                    costLeft -= if (!fake) {
                        Mishap.trulyHurt(this.ctx.caster, HexDamageSources.OVERCAST, healthToRemove.toFloat())

                        val actuallyTaken = Mth.ceil(mediaAbleToCastFromHP - (this.ctx.caster.health * mediaToHealth))

                        HexAdvancementTriggers.OVERCAST_TRIGGER.trigger(this.ctx.caster, actuallyTaken)
                        this.ctx.caster.awardStat(HexStatistics.MEDIA_OVERCAST, mediaCost - costLeft)
                        actuallyTaken
                    } else {
                        mediaToActuallyPayFor
                    }
                }
            }
        }

        if (!fake) {
            // this might be more than the media cost! for example if we waste a lot of media from an item
            this.ctx.caster.awardStat(HexStatistics.MEDIA_USED, mediaCost - costLeft)
            HexAdvancementTriggers.SPEND_MEDIA_TRIGGER.trigger(
                this.ctx.caster,
                mediaCost - costLeft,
                if (costLeft < 0) -costLeft else 0
            )
        }

        return if (fake) 0 else costLeft
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
        fun fromNBT(nbt: CompoundTag, ctx: CastingContext): CastingHarness {
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

    data class CastResult(
        val continuation: SpellContinuation,
        val newData: FunctionalData?,
        val resolutionType: ResolvedPatternType,
        val sideEffects: List<OperatorSideEffect>,
        val sound: EvalSound,
    )
}
