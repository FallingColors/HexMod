package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.item.DataHolder
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.common.items.magic.ItemPackagedSpell

class OpErase : SpellOperator {
    override val argc = 0

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val otherHandStack = ctx.caster.getItemInHand(ctx.otherHand)
        val otherHandItem = otherHandStack.item
        if (otherHandItem !is ItemPackagedSpell &&
            (otherHandItem !is DataHolder ||
                    !otherHandStack.hasTag() ||
                    otherHandItem.canWrite(otherHandStack.orCreateTag, null))) {
            throw MishapBadOffhandItem.of(otherHandStack, "eraseable")
        }

        return Triple(Spell, 10_000, listOf())
    }

    private object Spell : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val otherHandStack = ctx.caster.getItemInHand(ctx.otherHand)
            val otherHandItem = otherHandStack.item
            if (otherHandStack.hasTag()) {
                val tag = otherHandStack.orCreateTag
                if (otherHandItem is ItemPackagedSpell) {
                    tag.remove(ItemPackagedSpell.TAG_MANA)
                    tag.remove(ItemPackagedSpell.TAG_MAX_MANA)
                    tag.remove(ItemPackagedSpell.TAG_PATTERNS)
                } else if (otherHandItem is DataHolder && otherHandItem.canWrite(tag, null)) {
                    otherHandItem.writeDatum(tag, null)
                }
            }
        }
    }
}
