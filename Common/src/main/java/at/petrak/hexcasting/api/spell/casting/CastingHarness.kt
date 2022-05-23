package at.petrak.hexcasting.api.spell.casting

import at.petrak.hexcasting.api.PatternRegistry
import at.petrak.hexcasting.api.advancements.HexAdvancementTriggers
import at.petrak.hexcasting.api.block.circle.BlockEntityAbstractImpetus
import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.misc.HexDamageSources
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.mod.HexItemTags
import at.petrak.hexcasting.api.mod.HexStatistics
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellList
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.spell.mishaps.Mishap
import at.petrak.hexcasting.api.spell.mishaps.MishapDisallowedSpell
import at.petrak.hexcasting.api.spell.mishaps.MishapError
import at.petrak.hexcasting.api.spell.mishaps.MishapTooManyCloseParens
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.math.HexDir
import at.petrak.hexcasting.api.spell.mishaps.*
import at.petrak.hexcasting.api.utils.ManaHelper
import at.petrak.hexcasting.xplat.IXplatAbstractions
import at.petrak.hexcasting.api.utils.asCompound
import at.petrak.hexcasting.api.utils.getList
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3
import kotlin.math.min

/**
 * Keeps track of a player casting a spell on the server.
 * It's stored as NBT on the wand.
 */
class CastingHarness private constructor(
    var stack: MutableList<SpellDatum<*>>,
    var localIota: SpellDatum<*>,
    var parenCount: Int,
    var parenthesized: List<SpellDatum<*>>,
    var escapeNext: Boolean,
    val ctx: CastingContext,
    val prepackagedColorizer: FrozenColorizer? // for trinkets with colorizers
) {

    @JvmOverloads
    constructor(
        ctx: CastingContext,
        prepackagedColorizer: FrozenColorizer? = null
    ) : this(mutableListOf(), SpellDatum.make(Widget.NULL), 0, mutableListOf(), false, ctx, prepackagedColorizer)

    /**
     * Given a list of iotas, execute them in sequence.
     */
    fun executeIotas(iotas: List<SpellDatum<*>>, world: ServerLevel): ControllerInfo {
        val continuation: MutableList<ContinuationFrame> = mutableListOf(ContinuationFrame.Evaluate(SpellList.LList(0, iotas)))
        val info = TempControllerInfo(false, false, false)
        while (continuation.isNotEmpty() && !info.haveWeFuckedUp) {
            val next = continuation.removeLast()
            val result = next.evaluate(continuation, world, this)
            if (result.newData != null) {
                this.applyFunctionalData(result.newData)
            }
            performSideEffects(info, result.sideEffects)
        }

        

        return ControllerInfo(
            info.spellCast,
            info.playSound,
            this.stack.isEmpty() && this.parenCount == 0 && !this.escapeNext,
            info.haveWeFuckedUp,
            generateDescs()
        )
    }

    fun getUpdate(iota: SpellDatum<*>, world: ServerLevel, continuation: MutableList<ContinuationFrame>): CastResult {
        try {
            // wouldn't it be nice to be able to go paren'
            // i guess i'll call it paren2
            val paren2 = this.handleParentheses(iota)
            if (paren2 != null) {
                return CastResult(
                    paren2,
                    listOf()
                )
            }

            return if (iota.getType() == DatumType.PATTERN) {
                updateWithPattern(iota.payload as HexPattern, world, continuation)
            } else {
                CastResult(
                    null,
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
                null,
                listOf(OperatorSideEffect.DoMishap(mishap, Mishap.Context(iota.payload as? HexPattern ?: HexPattern(HexDir.WEST), null))),
            )
        } catch (exception: Exception) {
            exception.printStackTrace()
            return CastResult(
                null,
                listOf(OperatorSideEffect.DoMishap(MishapError(exception), Mishap.Context(iota.payload as? HexPattern ?: HexPattern(HexDir.WEST), null)))
            )
        }
    }

    /**
     * When the server gets a packet from the client with a new pattern,
     * handle it functionally.
     */
    fun updateWithPattern(newPat: HexPattern, world: ServerLevel, continuation: MutableList<ContinuationFrame>): CastResult {
        if (this.ctx.spellCircle == null)
            this.ctx.caster.awardStat(HexStatistics.PATTERNS_DRAWN)

        var operatorIdPair: Pair<Operator, ResourceLocation>? = null
        try {

            // Don't catch this one
            operatorIdPair = PatternRegistry.matchPatternAndID(newPat, world)
            if (!HexConfig.server().isActionAllowed(operatorIdPair.second)) {
                throw MishapDisallowedSpell()
            }
            val (stack2, local2, sideEffectsUnmut) = operatorIdPair.first.operate(continuation, this.stack.toMutableList(), this.localIota, this.ctx)
            this.localIota = local2
            // Stick a poofy particle effect at the caster position
            val sideEffects = sideEffectsUnmut.toMutableList()
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

            val fd = this.getFunctionalData().copy(
                stack = stack2,
            )

            return CastResult(
                fd,
                sideEffects,
            )
        } catch (mishap: Mishap) {
            return CastResult(
                null,
                listOf(OperatorSideEffect.DoMishap(mishap, Mishap.Context(newPat, operatorIdPair?.second))),
            )
        } catch (exception: Exception) {
            exception.printStackTrace()
            return CastResult(
                null,
                listOf(OperatorSideEffect.DoMishap(MishapError(exception), Mishap.Context(newPat, operatorIdPair?.second)))
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
                info.haveWeFuckedUp = true
                break
            }


            if (haskellProgrammersShakingandCryingRN is OperatorSideEffect.AttemptSpell) {
                info.spellCast = true
                if (haskellProgrammersShakingandCryingRN.hasCastingSound)
                    info.playSound = true
            }
        }
    }

    fun generateDescs(): List<Component> {
        val descs = ArrayList<Component>(this.stack.size)
        for (datum in this.stack) {
            descs.add(datum.display())
        }
        return descs
    }

    /**
     * Return the functional update represented by the current state (for use with `copy`)
     */
    fun getFunctionalData() = FunctionalData(
        this.stack.toList(),
        this.parenCount,
        this.parenthesized.toList(),
        this.escapeNext,
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
    }

    /**
     * Return a non-null value if we handled this in some sort of parenthesey way,
     * either escaping it onto the stack or changing the parenthese-handling state.
     */
    private fun handleParentheses(iota: SpellDatum<*>): FunctionalData? {
        val operator = (iota.payload as? HexPattern)?.let {
            try {
                PatternRegistry.matchPattern(it, this.ctx.world)
            } catch (mishap: Mishap) {
                null
            }
        }

        return if (this.parenCount > 0) {
            if (this.escapeNext) {
                val newParens = this.parenthesized.toMutableList()
                newParens.add(iota)
                this.getFunctionalData().copy(
                    escapeNext = false,
                    parenthesized = newParens
                )
            } else if (operator == Widget.ESCAPE) {
                this.getFunctionalData().copy(
                    escapeNext = true,
                )
            } else if (operator == Widget.OPEN_PAREN) {
                // we have escaped the parens onto the stack; we just also record our count.
                val newParens = this.parenthesized.toMutableList()
                newParens.add(iota)
                this.getFunctionalData().copy(
                    parenthesized = newParens,
                    parenCount = this.parenCount + 1
                )
            } else if (operator == Widget.CLOSE_PAREN) {
                val newParenCount = this.parenCount - 1
                if (newParenCount == 0) {
                    val newStack = this.stack.toMutableList()
                    newStack.add(SpellDatum.make(this.parenthesized.toList()))
                    this.getFunctionalData().copy(
                        stack = newStack,
                        parenCount = newParenCount,
                        parenthesized = listOf()
                    )
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
                    )
                }
            } else {
                val newParens = this.parenthesized.toMutableList()
                newParens.add(iota)
                this.getFunctionalData().copy(
                    parenthesized = newParens
                )
            }
        } else if (this.escapeNext) {
            val newStack = this.stack.toMutableList()
            newStack.add(iota)
            this.getFunctionalData().copy(
                stack = newStack,
                escapeNext = false,
            )
        } else if (operator == Widget.ESCAPE) {
            this.getFunctionalData().copy(
                escapeNext = true
            )
        } else if (operator == Widget.OPEN_PAREN) {
            this.getFunctionalData().copy(
                parenCount = this.parenCount + 1
            )
        } else if (operator == Widget.CLOSE_PAREN) {
            throw MishapTooManyCloseParens()
        } else {
            null
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
        if (this.ctx.caster.isCreative || manaCost <= 0) return 0
        var costLeft = manaCost

        if (this.ctx.spellCircle != null) {
            val tile = this.ctx.world.getBlockEntity(this.ctx.spellCircle.impetusPos)
            if (tile is BlockEntityAbstractImpetus) {
                val manaAvailable = tile.mana
                val manaToTake = min(costLeft, manaAvailable)
                costLeft -= manaToTake
                tile.mana = manaAvailable - manaToTake
            }
        } else {
            val casterStack = this.ctx.caster.getItemInHand(this.ctx.castingHand)
            val casterManaHolder = IXplatAbstractions.INSTANCE.findManaHolder(casterStack)
            val casterHexHolder = IXplatAbstractions.INSTANCE.findHexHolder(casterStack)
            val ipsCanDrawFromInv = if (casterHexHolder != null) {
                if (casterManaHolder != null) {
                    val manaAvailable = casterManaHolder.mana
                    val manaToTake = min(costLeft, manaAvailable)
                    casterManaHolder.mana = manaAvailable - manaToTake
                    costLeft -= manaToTake
                }
                casterHexHolder.canDrawManaFromInventory()
            } else {
                false
            }
            if (casterStack.`is`(HexItemTags.WANDS) || ipsCanDrawFromInv) {
                val manableItems = this.ctx.caster.inventory.items
                    .filter(ManaHelper::isManaItem)
                    .sortedWith(Comparator(ManaHelper::compare).reversed())
                for (stack in manableItems) {
                    costLeft -= ManaHelper.extractMana(stack, costLeft)
                    if (costLeft <= 0)
                        break
                }

                if (allowOvercast && costLeft > 0) {
                    // Cast from HP!
                    val manaToHealth = HexConfig.common().manaToHealthRate()
                    val healthtoRemove = costLeft.toDouble() / manaToHealth
                    val manaAbleToCastFromHP = this.ctx.caster.health * manaToHealth

                    val manaToActuallyPayFor = min(manaAbleToCastFromHP.toInt(), costLeft)
                    HexAdvancementTriggers.OVERCAST_TRIGGER.trigger(this.ctx.caster, manaToActuallyPayFor)
                    this.ctx.caster.awardStat(HexStatistics.MANA_OVERCASTED, manaCost - costLeft)

                    Mishap.trulyHurt(this.ctx.caster, HexDamageSources.OVERCAST, healthtoRemove.toFloat())
                    costLeft -= manaToActuallyPayFor
                }
            }
        }

        // this might be more than the mana cost! for example if we waste a lot of mana from an item
        this.ctx.caster.awardStat(HexStatistics.MANA_USED, manaCost - costLeft)
        HexAdvancementTriggers.SPEND_MANA_TRIGGER.trigger(
            this.ctx.caster,
            manaCost - costLeft,
            if (costLeft < 0) -costLeft else 0
        )

        return costLeft
    }

    fun getColorizer(): FrozenColorizer {
        if (this.prepackagedColorizer != null)
            return this.prepackagedColorizer

        return IXplatAbstractions.INSTANCE.getColorizer(this.ctx.caster)
    }


    fun serializeToNBT(): CompoundTag {
        val out = CompoundTag()

        val stackTag = ListTag()
        for (datum in this.stack)
            stackTag.add(datum.serializeToNBT())
        out.put(TAG_STACK, stackTag)

        out.put(TAG_LOCAL, localIota.serializeToNBT())

        out.putInt(TAG_PAREN_COUNT, this.parenCount)
        out.putBoolean(TAG_ESCAPE_NEXT, this.escapeNext)

        val parensTag = ListTag()
        for (pat in this.parenthesized)
            parensTag.add(pat.serializeToNBT())
        out.put(TAG_PARENTHESIZED, parensTag)

        if (this.prepackagedColorizer != null) {
            out.put(TAG_PREPACKAGED_COLORIZER, this.prepackagedColorizer.serialize())
        }

        return out
    }

    companion object {
        const val TAG_STACK = "stack"
        const val TAG_LOCAL = "local"
        const val TAG_PAREN_COUNT = "open_parens"
        const val TAG_PARENTHESIZED = "parenthesized"
        const val TAG_ESCAPE_NEXT = "escape_next"
        const val TAG_PREPACKAGED_COLORIZER = "prepackaged_colorizer"

        @JvmStatic
        fun DeserializeFromNBT(nbt: CompoundTag, ctx: CastingContext): CastingHarness {
            return try {
                val stack = mutableListOf<SpellDatum<*>>()
                val stackTag = nbt.getList(TAG_STACK, Tag.TAG_COMPOUND)
                for (subtag in stackTag) {
                    val datum = SpellDatum.DeserializeFromNBT(subtag.asCompound, ctx.world)
                    stack.add(datum)
                }

                val localTag = nbt.getCompound(TAG_LOCAL)
                val localIota = if (localTag.size() == 1) SpellDatum.DeserializeFromNBT(localTag, ctx.world) else SpellDatum.make(Widget.NULL)

                val parenthesized = mutableListOf<SpellDatum<*>>()
                val parenTag = nbt.getList(TAG_PARENTHESIZED, Tag.TAG_COMPOUND)
                for (subtag in parenTag) {
                    if (subtag.asCompound.size() != 1)
                        parenthesized.add(SpellDatum.make(HexPattern.DeserializeFromNBT(subtag.asCompound)))
                    else
                        parenthesized.add(SpellDatum.DeserializeFromNBT(subtag.asCompound, ctx.world))
                }

                val parenCount = nbt.getInt(TAG_PAREN_COUNT)
                val escapeNext = nbt.getBoolean(TAG_ESCAPE_NEXT)

                val colorizer = if (nbt.contains(TAG_PREPACKAGED_COLORIZER)) {
                    FrozenColorizer.deserialize(nbt.getCompound(TAG_PREPACKAGED_COLORIZER))
                } else {
                    null
                }

                CastingHarness(stack, localIota, parenCount, parenthesized, escapeNext, ctx, colorizer)
            } catch (exn: Exception) {
                CastingHarness(ctx)
            }
        }
    }

    data class TempControllerInfo(
        var spellCast: Boolean,
        var playSound: Boolean,
        var haveWeFuckedUp: Boolean,
    )

    data class CastResult(
        val newData: FunctionalData?,
        val sideEffects: List<OperatorSideEffect>,
    )
}
