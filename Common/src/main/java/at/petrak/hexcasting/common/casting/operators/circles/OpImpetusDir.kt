package at.petrak.hexcasting.common.casting.operators.circles

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapNoSpellCircle

object OpImpetusDir : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
        val circle = ctx.spellCircle
        if (circle == null)
            throw MishapNoSpellCircle()

        val pos = circle.impetusPos
        val bs = ctx.world.getBlockState(pos)
        val dir = bs.getValue(BlockAbstractImpetus.FACING)
        return dir.step().asActionResult
    }
}
