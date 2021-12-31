package at.petrak.hex.common.casting.operators.spells

import at.petrak.hex.api.Operator.Companion.getChecked
import at.petrak.hex.api.SpellOperator
import at.petrak.hex.common.casting.CastException
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.RenderedSpell
import at.petrak.hex.common.casting.SpellDatum
import at.petrak.hex.common.items.ItemPackagedSpell
import at.petrak.hex.hexmath.HexPattern
import net.minecraft.nbt.ListTag

class OpMakePackagedSpell<T : ItemPackagedSpell>(val type: Class<T>, val cost: Int) : SpellOperator {
    override val argc = 1
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Pair<RenderedSpell, Int> {
        val otherHandItem = ctx.caster.getItemInHand(ctx.otherHand)
        if (!type.isAssignableFrom(otherHandItem.item.javaClass)) {
            throw CastException(CastException.Reason.BAD_OFFHAND_ITEM, type, otherHandItem)
        }

        val patterns = args.getChecked<List<SpellDatum<*>>>(0).map { it.tryGet<HexPattern>() }
        return Pair(Spell(patterns), cost)
    }

    private data class Spell(val patterns: List<HexPattern>) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val otherHandItem = ctx.caster.getItemInHand(ctx.otherHand)
            if (otherHandItem.item is ItemPackagedSpell) {
                val tag = otherHandItem.orCreateTag
                val patsTag = ListTag()
                for (pat in patterns) {
                    patsTag.add(pat.serializeToNBT())
                }
                tag.put(ItemPackagedSpell.TAG_PATTERNS, patsTag)
            }
        }
    }
}