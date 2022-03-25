package at.petrak.hexcasting.common.casting.operators.circles

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.circle.BlockAbstractImpetus
import at.petrak.hexcasting.common.casting.CastException
import at.petrak.hexcasting.common.casting.CastingContext
import net.minecraft.world.phys.Vec3

object OpImpetusDir : ConstManaOperator {
    override val argc = 0

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        if (ctx.spellCircle == null)
            throw CastException(CastException.Reason.NO_SPELL_CIRCLE)

        val pos = ctx.spellCircle.impetusPos
        val bs = ctx.world.getBlockState(pos)
        val dir = bs.getValue(BlockAbstractImpetus.FACING)
        return Operator.spellListOf(Vec3(dir.step()))
    }
}