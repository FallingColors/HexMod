package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.item.DataHolder
import at.petrak.hexcasting.api.item.SpellHolder
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
        val (handStack, hand) = ctx.getHeldItemToOperateOn {
            val item = it.item
            (item is SpellHolder && item.getPatterns(it) != null) ||
                    (item is DataHolder && it.hasTag() && item.canWrite(it, null))
        }
        val handItem = handStack.item
        if ((handItem !is SpellHolder || handItem.getPatterns(handStack) == null) &&
            (handItem !is DataHolder ||
                    !handStack.hasTag() ||
                    !handItem.canWrite(handStack, null))) {
            throw MishapBadOffhandItem.of(handStack, hand, "eraseable")
        }

        return Triple(Spell, 10_000, listOf())
    }

    private object Spell : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val (handStack) = ctx.getHeldItemToOperateOn {
                val item = it.item
                (item is SpellHolder && item.getPatterns(it) != null) ||
                        (item is DataHolder && it.hasTag() && item.canWrite(it, null))
            }
            val handItem = handStack.item
            if (handStack.hasTag()) {
                if (handItem is SpellHolder && handItem.getPatterns(handStack) != null) {
                    handItem.clearPatterns(handStack)
                } else if (handItem is DataHolder && handItem.canWrite(handStack, null)) {
                    handItem.writeDatum(handStack, null)
                }
            }
        }
    }
}
