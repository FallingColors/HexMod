package at.petrak.hexcasting.common.casting

import at.petrak.hexcasting.HexConfig
import at.petrak.hexcasting.HexMod
import at.petrak.hexcasting.api.PatternRegistry
import at.petrak.hexcasting.api.circle.BlockEntityAbstractImpetus
import at.petrak.hexcasting.api.item.SpellHolder
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.casting.colors.FrozenColorizer
import at.petrak.hexcasting.common.casting.mishaps.Mishap
import at.petrak.hexcasting.common.casting.mishaps.MishapTooManyCloseParens
import at.petrak.hexcasting.common.items.ItemWand
import at.petrak.hexcasting.common.items.magic.ItemPackagedSpell
import at.petrak.hexcasting.common.lib.HexCapabilities
import at.petrak.hexcasting.common.lib.HexDamageSources
import at.petrak.hexcasting.common.lib.HexPlayerDataHelper
import at.petrak.hexcasting.common.lib.HexStatistics
import at.petrak.hexcasting.datagen.HexAdvancements
import at.petrak.hexcasting.hexmath.HexPattern
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
    var parenCount: Int,
    var parenthesized: List<HexPattern>,
    var escapeNext: Boolean,
    val ctx: CastingContext,
    val prepackagedColorizer: FrozenColorizer? // for trinkets with colorizers
) {

    @JvmOverloads
    constructor(
        ctx: CastingContext,
        prepackagedColorizer: FrozenColorizer? = null
    ) : this(mutableListOf(), 0, mutableListOf(), false, ctx, prepackagedColorizer)

    /**
     * Given a pattern, do all the updating/side effects/etc required.
     */
    fun executeNewPattern(newPat: HexPattern, world: ServerLevel): ControllerInfo {
        val result = this.getUpdate(newPat, world)
        this.applyFunctionalData(result.newData)
        return this.performSideEffects(result.sideEffects)
    }

    /**
     * When the server gets a packet from the client with a new pattern,
     * handle it functionally.
     */
    fun getUpdate(newPat: HexPattern, world: ServerLevel): CastResult {
        if (this.ctx.spellCircle == null)
            this.ctx.caster.awardStat(HexStatistics.PATTERNS_DRAWN)

        var operatorIdPair: Pair<Operator, ResourceLocation>? = null
        try {
            // wouldn't it be nice to be able to go paren'
            // i guess i'll call it paren2
            val paren2 = this.handleParentheses(newPat)
            if (paren2 != null) {
                return CastResult(
                    paren2,
                    listOf(),
                )
            }

            // Don't catch this one
            operatorIdPair = PatternRegistry.matchPatternAndID(newPat, world)
            val (stack2, sideEffectsUnmut) = operatorIdPair.first.operate(this.stack.toMutableList(), this.ctx)
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
                this.getFunctionalData(),
                listOf(OperatorSideEffect.DoMishap(mishap, Mishap.Context(newPat, operatorIdPair?.second))),
            )
        }
    }

    /**
     * Execute the side effects of a cast, and then tell the client what to think about it.
     */
    fun performSideEffects(sideEffects: List<OperatorSideEffect>): ControllerInfo {
        var wasSpellCast = false
        var wasPrevPatternInvalid = false
        var hasCastingSound = false
        for (haskellProgrammersShakingandCryingRN in sideEffects) {
            val mustStop = haskellProgrammersShakingandCryingRN.performEffect(this)
            if (mustStop) {
                wasPrevPatternInvalid = true
                break
            }


            if (haskellProgrammersShakingandCryingRN is OperatorSideEffect.AttemptSpell) {
                wasSpellCast = true
                if (haskellProgrammersShakingandCryingRN.hasCastingSound)
                    hasCastingSound = true
            }
        }

        return ControllerInfo(
            wasSpellCast,
            hasCastingSound,
            this.stack.isEmpty() && this.parenCount == 0 && !this.escapeNext,
            wasPrevPatternInvalid,
            generateDescs()
        )
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
    private fun getFunctionalData() = FunctionalData(
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
    private fun handleParentheses(newPat: HexPattern): FunctionalData? {
        val operator = try {
            PatternRegistry.matchPattern(newPat, this.ctx.world)
        } catch (mishap: Mishap) {
            null
        }

        return if (this.parenCount > 0) {
            if (this.escapeNext) {
                val newParens = this.parenthesized.toMutableList()
                newParens.add(newPat)
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
                newParens.add(newPat)
                this.getFunctionalData().copy(
                    parenthesized = newParens,
                    parenCount = this.parenCount + 1
                )
            } else if (operator == Widget.CLOSE_PAREN) {
                val newParenCount = this.parenCount - 1
                if (newParenCount == 0) {
                    val newStack = this.stack.toMutableList()
                    newStack.add(SpellDatum.make(this.parenthesized.map { SpellDatum.make(it) }))
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
                    newParens.add(newPat)
                    this.getFunctionalData().copy(
                        parenCount = newParenCount,
                        parenthesized = newParens
                    )
                }
            } else {
                val newParens = this.parenthesized.toMutableList()
                newParens.add(newPat)
                this.getFunctionalData().copy(
                    parenthesized = newParens
                )
            }
        } else if (this.escapeNext) {
            val newStack = this.stack.toMutableList()
            newStack.add(SpellDatum.make(newPat))
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
            } else {
                HexMod.getLogger()
                    .warn("There was supposed to be an impetus at ${this.ctx.spellCircle.impetusPos.toShortString()}")
            }
        } else {
            val casterStack = this.ctx.caster.getItemInHand(this.ctx.castingHand)
            val casterItem = casterStack.item
            val ipsCanDrawFromInv = if (casterItem is SpellHolder) {
                val mana = casterStack.getCapability(HexCapabilities.MANA).resolve().get()
                val manaAvailable = mana.mana
                val manaToTake = min(costLeft, manaAvailable)
                mana.mana = manaAvailable - manaToTake
                costLeft -= manaToTake
                casterItem.canDrawManaFromInventory(casterStack)
            } else {
                false
            }
            if (casterItem is ItemWand || ipsCanDrawFromInv) {
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
                    val manaToHealth = HexConfig.manaToHealthRate.get()
                    val healthtoRemove = costLeft.toDouble() / manaToHealth
                    val manaAbleToCastFromHP = this.ctx.caster.health * manaToHealth

                    val manaToActuallyPayFor = min(manaAbleToCastFromHP.toInt(), costLeft)
                    HexAdvancements.OVERCAST_TRIGGER.trigger(this.ctx.caster, manaToActuallyPayFor)
                    this.ctx.caster.awardStat(HexStatistics.MANA_OVERCASTED, manaCost - costLeft)

                    this.ctx.caster.hurt(HexDamageSources.OVERCAST, healthtoRemove.toFloat())
                    costLeft -= manaToActuallyPayFor
                }
            }
        }

        // this might be more than the mana cost! for example if we waste a lot of mana from an item
        this.ctx.caster.awardStat(HexStatistics.MANA_USED, manaCost - costLeft)
        HexAdvancements.SPEND_MANA_TRIGGER.trigger(
            this.ctx.caster,
            manaCost - costLeft,
            if (costLeft < 0) -costLeft else 0
        )

        return costLeft
    }

    fun getColorizer(): FrozenColorizer {
        if (this.prepackagedColorizer != null)
            return this.prepackagedColorizer

        return HexPlayerDataHelper.getColorizer(ctx.caster)
    }


    fun serializeToNBT(): CompoundTag {
        val out = CompoundTag()

        val stackTag = ListTag()
        for (datum in this.stack)
            stackTag.add(datum.serializeToNBT())
        out.put(TAG_STACK, stackTag)

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
        const val TAG_PAREN_COUNT = "open_parens"
        const val TAG_PARENTHESIZED = "parenthesized"
        const val TAG_ESCAPE_NEXT = "escape_next"
        const val TAG_PREPACKAGED_COLORIZER = "prepackaged_colorizer"

        @JvmStatic
        fun DeserializeFromNBT(nbt: Tag, ctx: CastingContext): CastingHarness {
            return try {
                val nbt = nbt as CompoundTag

                val stack = mutableListOf<SpellDatum<*>>()
                val stackTag = nbt.getList(TAG_STACK, Tag.TAG_COMPOUND.toInt())
                for (subtag in stackTag) {
                    val datum = SpellDatum.DeserializeFromNBT(subtag as CompoundTag, ctx)
                    stack.add(datum)
                }

                val parenthesized = mutableListOf<HexPattern>()
                val parenTag = nbt.getList(TAG_PARENTHESIZED, Tag.TAG_COMPOUND.toInt())
                for (subtag in parenTag) {
                    parenthesized.add(HexPattern.DeserializeFromNBT(subtag as CompoundTag))
                }

                val parenCount = nbt.getInt(TAG_PAREN_COUNT)
                val escapeNext = nbt.getBoolean(TAG_ESCAPE_NEXT)

                val colorizer = if (nbt.contains(TAG_PREPACKAGED_COLORIZER)) {
                    FrozenColorizer.deserialize(nbt.getCompound(TAG_PREPACKAGED_COLORIZER))
                } else {
                    null
                }

                CastingHarness(stack, parenCount, parenthesized, escapeNext, ctx, colorizer)
            } catch (exn: Exception) {
                HexMod.LOGGER.warn("Couldn't load harness from nbt tag, falling back to default: $nbt: $exn")
                CastingHarness(ctx)
            }
        }
    }

    data class CastResult(
        val newData: FunctionalData,
        val sideEffects: List<OperatorSideEffect>,
    )
}
