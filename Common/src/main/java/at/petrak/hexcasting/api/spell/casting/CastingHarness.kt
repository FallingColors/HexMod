package at.petrak.hexcasting.api.spell.casting

import at.petrak.hexcasting.api.PatternRegistry
import at.petrak.hexcasting.api.advancements.HexAdvancementTriggers
import at.petrak.hexcasting.api.block.circle.BlockEntityAbstractImpetus
import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.misc.HexDamageSources
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.mod.HexItemTags
import at.petrak.hexcasting.api.mod.HexStatistics
import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.SpellList
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.ListIota
import at.petrak.hexcasting.api.spell.iota.PatternIota
import at.petrak.hexcasting.api.spell.math.HexDir
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.spell.mishaps.*
import at.petrak.hexcasting.api.utils.*
import at.petrak.hexcasting.common.items.magic.ItemCreativeUnlocker
import at.petrak.hexcasting.common.lib.HexIotaTypes
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3
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

    /**
     * Given a list of iotas, execute them in sequence.
     */
    fun executeIotas(iotas: List<Iota>, world: ServerLevel): ControllerInfo {
        // Initialize the continuation stack to a single top-level eval for all iotas.
        var continuation = SpellContinuation.Done.pushFrame(ContinuationFrame.Evaluate(SpellList.LList(0, iotas)))
        // Begin aggregating info
        val info = TempControllerInfo(playSound = false, earlyExit = false)
        var lastResolutionType = ResolvedPatternType.UNRESOLVED
        while (continuation is SpellContinuation.NotDone && !info.earlyExit) {
            // Take the top of the continuation stack...
            val next = continuation.frame
            // ...and execute it.
            val result = next.evaluate(continuation.next, world, this)
            // Then write all pertinent data back to the harness for the next iteration.
            if (result.newData != null) {
                this.applyFunctionalData(result.newData)
            }
            continuation = result.continuation
            lastResolutionType = result.resolutionType
            performSideEffects(info, result.sideEffects)
            info.earlyExit = info.earlyExit || !lastResolutionType.success
        }

        if (continuation is SpellContinuation.NotDone) {
            lastResolutionType =
                if (lastResolutionType.success) ResolvedPatternType.EVALUATED else ResolvedPatternType.ERRORED
        }

        val (stackDescs, parenDescs, ravenmind) = generateDescs()

        return ControllerInfo(
            info.playSound,
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
            this.handleParentheses(iota)?.let { (data, resolutionType) ->
                return@getUpdate CastResult(continuation, data, resolutionType, listOf())
            }

            return if (iota is PatternIota) {
                updateWithPattern(iota.pattern, world, continuation)
            } else {
                CastResult(
                    continuation,
                    null,
                    ResolvedPatternType.INVALID, // Should never matter
                    listOf(
                        OperatorSideEffect.DoMishap(
                            MishapUnescapedValue(iota),
                            Mishap.Context(HexPattern(HexDir.WEST), null)
                        )
                    ),
                )
            }
        } catch (mishap: Mishap) {
            return CastResult(
                continuation,
                null,
                mishap.resolutionType(ctx),
                listOf(
                    OperatorSideEffect.DoMishap(
                        mishap,
                        Mishap.Context((iota as? PatternIota)?.pattern ?: HexPattern(HexDir.WEST), null)
                    )
                ),
            )
        } catch (exception: Exception) {
            exception.printStackTrace()
            return CastResult(
                continuation,
                null,
                ResolvedPatternType.ERRORED,
                listOf(
                    OperatorSideEffect.DoMishap(
                        MishapError(exception),
                        Mishap.Context((iota as? PatternIota)?.pattern ?: HexPattern(HexDir.WEST), null)
                    )
                )
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
            actionIdPair = PatternRegistry.matchPatternAndID(newPat, world)
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
            var ravenmindChanged = false

            if (!unenlightened || pattern.alwaysProcessGreatSpell) {
                val result = pattern.operate(
                    continuation,
                    this.stack.toMutableList(),
                    this.ravenmind,
                    this.ctx
                )
                cont2 = result.newContinuation
                stack2 = result.newStack
                ravenmind2 = result.newRavenmind
                ravenmindChanged = true
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
            val fd = if (stack2 != null || ravenmindChanged) {
                hereFd.copy(
                    stack = stack2 ?: hereFd.stack,
                    ravenmind = if (ravenmindChanged) ravenmind2 else hereFd.ravenmind
                )
            } else {
                hereFd
            }

            return CastResult(
                cont2,
                fd,
                ResolvedPatternType.EVALUATED,
                sideEffects,
            )

        } catch (mishap: Mishap) {
            return CastResult(
                continuation,
                null,
                mishap.resolutionType(ctx),
                listOf(OperatorSideEffect.DoMishap(mishap, Mishap.Context(newPat, actionIdPair?.second))),
            )
        } catch (exception: Exception) {
            exception.printStackTrace()
            return CastResult(
                continuation,
                null,
                ResolvedPatternType.ERRORED,
                listOf(
                    OperatorSideEffect.DoMishap(
                        MishapError(exception),
                        Mishap.Context(newPat, actionIdPair?.second)
                    )
                )
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

            if (haskellProgrammersShakingandCryingRN is OperatorSideEffect.AttemptSpell &&
                haskellProgrammersShakingandCryingRN.hasCastingSound
            ) {
                info.playSound = true
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
        if (iota !is PatternIota) {
            throw IllegalStateException()
        }
        val sig = iota.pattern.anglesSignature()

        if (this.parenCount > 0) {
            if (this.escapeNext) {
                val newParens = this.parenthesized.toMutableList()
                newParens.add(iota)
                return this.getFunctionalData().copy(
                    escapeNext = false,
                    parenthesized = newParens
                ) to ResolvedPatternType.ESCAPED
            }

            return when (sig) {
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

        if (this.escapeNext) {
            val newStack = this.stack.toMutableList()
            newStack.add(iota)
            return this.getFunctionalData().copy(
                stack = newStack,
                escapeNext = false,
            ) to ResolvedPatternType.ESCAPED
        }

        return when (sig) {
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

    /**
     * Might cast from hitpoints.
     * Returns the mana cost still remaining after we deplete everything. It will be <= 0 if we could pay for it.
     *
     * Also awards stats and achievements and such
     */
    fun withdrawMana(manaCost: Int, allowOvercast: Boolean): Int {
        // prevent poor impls from gaining you mana
        if (manaCost <= 0) return 0
        var costLeft = manaCost

        val fake = this.ctx.caster.isCreative

        if (this.ctx.spellCircle != null) {
            if (fake)
                return 0

            val tile = this.ctx.world.getBlockEntity(this.ctx.spellCircle.impetusPos)
            if (tile is BlockEntityAbstractImpetus) {
                val manaAvailable = tile.mana
                if (manaAvailable < 0)
                    return 0

                val manaToTake = min(costLeft, manaAvailable)
                costLeft -= manaToTake
                tile.mana = manaAvailable - manaToTake
            }
        } else {
            val casterStack = this.ctx.caster.getItemInHand(this.ctx.castingHand)
            val casterManaHolder = IXplatAbstractions.INSTANCE.findManaHolder(casterStack)
            val casterHexHolder = IXplatAbstractions.INSTANCE.findHexHolder(casterStack)
            val hexHolderDrawsFromInventory = if (casterHexHolder != null) {
                if (casterManaHolder != null) {
                    val manaAvailable = casterManaHolder.media
                    val manaToTake = min(costLeft, manaAvailable)
                    if (!fake) casterManaHolder.media = manaAvailable - manaToTake
                    costLeft -= manaToTake
                }
                casterHexHolder.canDrawManaFromInventory()
            } else {
                false
            }
            if (casterStack.`is`(HexItemTags.STAVES) || hexHolderDrawsFromInventory) {
                val manableItems = this.ctx.caster.inventory.items
                    .filter(::isManaItem)
                    .sortedWith(Comparator(::compareManaItem).reversed())
                for (stack in manableItems) {
                    costLeft -= extractMana(stack, costLeft, simulate = fake && !ItemCreativeUnlocker.isDebug(stack))
                    if (costLeft <= 0)
                        break
                }

                if (allowOvercast && costLeft > 0) {
                    // Cast from HP!
                    val manaToHealth = HexConfig.common().manaToHealthRate()
                    val healthtoRemove = costLeft.toDouble() / manaToHealth
                    val manaAbleToCastFromHP = this.ctx.caster.health * manaToHealth

                    val manaToActuallyPayFor = min(manaAbleToCastFromHP.toInt(), costLeft)
                    if (!fake) {
                        HexAdvancementTriggers.OVERCAST_TRIGGER.trigger(this.ctx.caster, manaToActuallyPayFor)
                        this.ctx.caster.awardStat(HexStatistics.MANA_OVERCASTED, manaCost - costLeft)

                        Mishap.trulyHurt(this.ctx.caster, HexDamageSources.OVERCAST, healthtoRemove.toFloat())
                    }
                    costLeft -= manaToActuallyPayFor
                }
            }
        }

        if (!fake) {
            // this might be more than the mana cost! for example if we waste a lot of mana from an item
            this.ctx.caster.awardStat(HexStatistics.MANA_USED, manaCost - costLeft)
            HexAdvancementTriggers.SPEND_MANA_TRIGGER.trigger(
                this.ctx.caster,
                manaCost - costLeft,
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
        var playSound: Boolean,
        var earlyExit: Boolean,
    )

    data class CastResult(
        val continuation: SpellContinuation,
        val newData: FunctionalData?,
        val resolutionType: ResolvedPatternType,
        val sideEffects: List<OperatorSideEffect>,
    )
}
