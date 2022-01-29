package at.petrak.hexcasting.common.casting

import at.petrak.hexcasting.HexMod
import at.petrak.hexcasting.api.PatternRegistry
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.common.items.HexItems
import at.petrak.hexcasting.common.items.ItemWand
import at.petrak.hexcasting.common.items.magic.ItemPackagedSpell
import at.petrak.hexcasting.common.lib.HexCapabilities
import at.petrak.hexcasting.common.lib.HexDamageSources
import at.petrak.hexcasting.common.lib.HexStatistics
import at.petrak.hexcasting.datagen.Advancements
import at.petrak.hexcasting.hexmath.HexPattern
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import java.util.*
import kotlin.math.min
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * Keeps track of a player casting a spell on the server.
 * It's stored as NBT on the wand.
 */
class CastingHarness private constructor(
    val stack: MutableList<SpellDatum<*>>,
    var parenCount: Int,
    var parenthesized: MutableList<HexPattern>,
    var escapeNext: Boolean,
    val ctx: CastingContext,
    val prepackagedColorizer: ItemStack?
) {

    constructor(ctx: CastingContext) : this(mutableListOf(), 0, mutableListOf(), false, ctx, null)


    /**
     * When the server gets a packet from the client with a new pattern,
     * handle it.
     */
    fun update(newPat: HexPattern, world: ServerLevel): CastResult {
        return try {
            var exn: CastException? = null
            val operator = try {
                PatternRegistry.matchPattern(newPat, world)
            } catch (e: CastException) {
                exn = e
                null
            }
            if (this.parenCount > 0) {
                if (this.escapeNext) {
                    this.escapeNext = false
                    this.parenthesized.add(newPat)
                } else if (operator == Widget.ESCAPE) {
                    this.escapeNext = true
                } else if (operator == Widget.OPEN_PAREN) {
                    // we have escaped the parens onto the stack; we just also record our count.
                    this.parenthesized.add(newPat)
                    this.parenCount++
                } else if (operator == Widget.CLOSE_PAREN) {
                    this.parenCount--
                    if (this.parenCount == 0) {
                        this.stack.add(SpellDatum.make(this.parenthesized.map { SpellDatum.make(it) }))
                        this.parenthesized.clear()
                    } else if (this.parenCount < 0) {
                        throw CastException(CastException.Reason.TOO_MANY_CLOSE_PARENS)
                    } else {
                        // we have this situation: "(()"
                        // we need to add the close paren
                        this.parenthesized.add(newPat)
                    }
                } else {
                    this.parenthesized.add(newPat)
                }
            } else if (this.escapeNext) {
                this.escapeNext = false
                this.stack.add(SpellDatum.make(newPat))
            } else if (operator == Widget.ESCAPE) {
                this.escapeNext = true
            } else if (exn != null) {
                // there was a problem finding the pattern and it was NOT due to numbers
                throw exn
            } else if (operator == Widget.OPEN_PAREN) {
                this.parenCount++
            } else if (operator == Widget.CLOSE_PAREN) {
                throw CastException(CastException.Reason.TOO_MANY_CLOSE_PARENS)
            } else {
                // we know the operator is ok here
                val (stackPrime, sideEffects) = operator!!.operate(this.stack, this.ctx)

                
            }

            if (spellsToCast.isNotEmpty()) {
                CastResult.Cast(spellsToCast, this.stack.isEmpty())
            } else if (this.stack.isEmpty() && !this.escapeNext) {
                if (this.parenCount == 0) {
                    CastResult.QuitCasting
                } else {
                    CastResult.Continue(false)
                }
            } else {
                CastResult.Continue(false)
            }
        } catch (e: CastException) {
            if (e.reason == CastException.Reason.INVALID_PATTERN) {
                val idx = Random.nextInt(0..this.stack.size)
                this.stack.add(idx, SpellDatum.make(Widget.GARBAGE))
                CastResult.Continue(true)
            } else {
                CastResult.Error(e)
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
        if (this.ctx.caster.isCreative || manaCost <= 0) return 0
        var costLeft = manaCost

        val casterStack = this.ctx.caster.getItemInHand(this.ctx.castingHand)
        val casterItem = casterStack.item
        val ipsCanDrawFromInv = if (casterItem is ItemPackagedSpell) {
            val tag = casterStack.orCreateTag
            val manaAvailable = tag.getInt(ItemPackagedSpell.TAG_MANA)
            val manaToTake = min(costLeft, manaAvailable)
            tag.putInt(ItemPackagedSpell.TAG_MANA, manaAvailable - manaToTake)
            costLeft -= manaToTake
            casterItem.canDrawManaFromInventory()
        } else {
            false
        }
        if (casterItem is ItemWand || ipsCanDrawFromInv) {
            val manableItems = this.ctx.caster.inventory.items
                .filter { !Objects.isNull(ManaHelper.priority(it)) }
                .sortedByDescending(ManaHelper::priority)
            for (stack in manableItems) {
                costLeft -= ManaHelper.extractMana(stack, costLeft)!!
                if (costLeft <= 0)
                    break
            }
        }

        if (allowOvercast && costLeft > 0) {
            // Cast from HP!
            val manaToHealth = HexMod.CONFIG.manaToHealthRate.get()
            val healthtoRemove = costLeft.toDouble() / manaToHealth
            val manaAbleToCastFromHP = this.ctx.caster.health * manaToHealth

            val manaToActuallyPayFor = min(manaAbleToCastFromHP.toInt(), costLeft)
            Advancements.OVERCAST_TRIGGER.trigger(this.ctx.caster, manaToActuallyPayFor)
            this.ctx.caster.awardStat(HexStatistics.MANA_OVERCASTED, manaCost - costLeft)

            this.ctx.caster.hurt(HexDamageSources.OVERCAST, healthtoRemove.toFloat())
            costLeft -= manaToActuallyPayFor
        }

        // this might be more than the mana cost! for example if we waste a lot of mana from an item
        this.ctx.caster.awardStat(HexStatistics.MANA_USED, manaCost - costLeft)
        Advancements.SPEND_MANA_TRIGGER.trigger(
            this.ctx.caster,
            manaCost - costLeft,
            if (costLeft < 0) -costLeft else 0
        )

        return costLeft
    }

    fun getColorizer(): ItemStack {
        if (this.prepackagedColorizer != null)
            return this.prepackagedColorizer

        val maybeCap = this.ctx.caster.getCapability(HexCapabilities.PREFERRED_COLORIZER).resolve()
        if (maybeCap.isEmpty) {
            // uh oh
            return ItemStack(HexItems.DYE_COLORIZERS[0].get())
        }
        return maybeCap.get().colorizer
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
            out.put(TAG_PREPACKAGED_COLORIZER, this.prepackagedColorizer.serializeNBT())
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
        fun DeserializeFromNBT(nbt: Tag?, caster: ServerPlayer, wandHand: InteractionHand): CastingHarness {
            val ctx = CastingContext(caster, wandHand)
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
                    ItemStack.of(nbt.getCompound(TAG_PREPACKAGED_COLORIZER))
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

    sealed class CastResult {
        /** Casting still in progress */
        data class Continue(val prevPatternBad: Boolean) : CastResult()

        /** Non-catastrophic quit */
        object QuitCasting : CastResult()

        /** Finished casting */
        data class Cast(val sideEffects: List<OperatorSideEffect>, val quit: Boolean) : CastResult()

        /** uh-oh */
        data class Error(val exn: CastException) : CastResult()

        /** YOU DIED due to casting too hard from hit points. */
        object Died : CastResult()

        fun quitStatus(): QuitStatus =
            when (this) {
                QuitCasting, Died, is Error -> QuitStatus.QUIT
                is Cast -> if (this.quit) QuitStatus.QUIT else QuitStatus.OK
                is Continue -> if (this.prevPatternBad) QuitStatus.LAST_PATTERN_INVALID else QuitStatus.OK
            }
    }

    enum class QuitStatus {
        OK,

        /** Keep going, but the pattern you just drew was sussy */
        LAST_PATTERN_INVALID,
        QUIT
    }
}