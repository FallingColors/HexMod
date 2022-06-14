package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getNumOrVec
import at.petrak.hexcasting.api.spell.iota.Iota

object OpMulDot : ConstManaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val lhs = args.getNumOrVec(0, OpAdd.argc)
        val rhs = args.getNumOrVec(1, OpAdd.argc)

        return lhs.map(
            { lnum ->
                rhs.map(
                    { rnum -> (lnum * rnum).asActionResult }, { rvec -> rvec.scale(lnum).asActionResult }
                )
            }, { lvec ->
                rhs.map(
                    { rnum -> lvec.scale(rnum).asActionResult }, { rvec -> lvec.dot(rvec).asActionResult }
                )
            })

    }
}
