package at.petrak.hexcasting.common.casting.operators.circles

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus
import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.LegacySpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapNoSpellCircle

object OpImpetusDir : ConstManaOperator {
    override val argc = 0
    
    override fun execute(args: List<LegacySpellDatum<*>>, ctx: CastingContext): List<LegacySpellDatum<*>> {
        if (ctx.spellCircle == null)
            throw MishapNoSpellCircle()

        val pos = ctx.spellCircle.impetusPos
        val bs = ctx.world.getBlockState(pos)
        val dir = bs.getValue(BlockAbstractImpetus.FACING)
        return dir.step().asSpellResult
    }
}
