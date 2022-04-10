package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.item.DataHolder
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.common.casting.mishaps.MishapOthersName
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Items

// we make this a spell cause imo it's a little ... anticlimactic for it to just make no noise
object OpWrite : SpellOperator {
    override val argc = 1
    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val datum = args[0]

        val handStack = ctx.getHeldItemToOperateOn {
            val item = it.item
            if (item is DataHolder) {
                val tag = it.orCreateTag
                item.canWrite(tag, datum)
            } else false
        }

        val handItem = handStack.item as? DataHolder ?: throw MishapBadOffhandItem.of(handStack, "iota.write")
        val tag = handStack.orCreateTag

        if (!handItem.canWrite(tag, datum))
            throw MishapBadOffhandItem.of(handStack, "iota.readonly", datum.display())

        val trueName = MishapOthersName.getTrueNameFromDatum(datum, ctx.caster)
        if (trueName != null)
            throw MishapOthersName(trueName)

        return Triple(
            Spell(datum),
            0,
            listOf()
        )
    }

    private data class Spell(val datum: SpellDatum<*>) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val handStack = ctx.getHeldItemToOperateOn {
                val item = it.item
                if (item is DataHolder) {
                    val tag = it.orCreateTag
                    item.canWrite(tag, datum)
                } else false
            }
            val handItem = handStack.item

            if (handItem is DataHolder) {
                val tag = handStack.orCreateTag
                handItem.writeDatum(tag, datum)
            }
        }

    }
}
