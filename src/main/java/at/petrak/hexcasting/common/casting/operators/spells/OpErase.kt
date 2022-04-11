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
        val handStack = ctx.getHeldItemToOperateOn {
            val item = it.item
            item is ItemPackagedSpell || (item is DataHolder && it.hasTag() && item.canWrite(it.orCreateTag, null))
        }
        val handItem = handStack.item
        if (handItem !is ItemPackagedSpell &&
            (handItem !is DataHolder ||
                    !handStack.hasTag() ||
                    handItem.canWrite(handStack.orCreateTag, null))) {
            throw MishapBadOffhandItem.of(handStack, "eraseable")
        }

        return Triple(Spell, 10_000, listOf())
    }

    private object Spell : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val handStack = ctx.getHeldItemToOperateOn {
                val item = it.item
                item is ItemPackagedSpell || (item is DataHolder && it.hasTag() && item.canWrite(it.orCreateTag, null))
            }
            val handItem = handStack.item
            if (handStack.hasTag()) {
                val tag = handStack.orCreateTag
                if (handItem is ItemPackagedSpell) {
                    tag.remove(ItemPackagedSpell.TAG_MANA)
                    tag.remove(ItemPackagedSpell.TAG_MAX_MANA)
                    tag.remove(ItemPackagedSpell.TAG_PATTERNS)
                } else if (handItem is DataHolder && handItem.canWrite(tag, null)) {
                    handItem.writeDatum(tag, null)
                }
            }
        }
    }
}
