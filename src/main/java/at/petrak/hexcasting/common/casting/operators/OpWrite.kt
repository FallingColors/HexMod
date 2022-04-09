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

// we make this a spell cause imo it's a little ... anticlimactic for it to just make no noise
object OpWrite : SpellOperator {
    override val argc = 1
    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val handStack = ctx.caster.getItemInHand(ctx.otherHand)
        val handItem = handStack.item
        val tag = handStack.orCreateTag
        val datum = args[0]

        val canWrite = if (handItem is DataHolder) {
            handItem.canWrite(tag, datum)
        } else {
            false
        }
        if (!canWrite)
            throw MishapBadOffhandItem.of(handStack, "iota.write")

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
            val handStack = ctx.caster.getItemInHand(ctx.otherHand)
            val handItem = handStack.item
            val tag = handStack.orCreateTag
            if (handItem is DataHolder) {
                handItem.writeDatum(tag, datum)
            } // else fuck
        }

    }
}
