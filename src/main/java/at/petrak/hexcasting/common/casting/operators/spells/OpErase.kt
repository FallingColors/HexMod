package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.ParticleSpray
import at.petrak.hexcasting.api.RenderedSpell
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.api.SpellOperator
import at.petrak.hexcasting.common.casting.CastException
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.items.magic.ItemPackagedSpell

class OpErase : SpellOperator {
    override val argc = 0

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val otherHandItem = ctx.caster.getItemInHand(ctx.otherHand)
        if (otherHandItem.item !is ItemPackagedSpell) {
            throw CastException(CastException.Reason.BAD_OFFHAND_ITEM, ItemPackagedSpell::class.java, otherHandItem)
        }

        return Triple(Spell, 10_000, listOf())
    }

    private object Spell : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val otherHandItem = ctx.caster.getItemInHand(ctx.otherHand)
            if (otherHandItem.item is ItemPackagedSpell && otherHandItem.hasTag()) {
                val tag = otherHandItem.orCreateTag
                tag.remove(ItemPackagedSpell.TAG_MANA)
                tag.remove(ItemPackagedSpell.TAG_MAX_MANA)
                tag.remove(ItemPackagedSpell.TAG_PATTERNS)
            }
        }
    }
}