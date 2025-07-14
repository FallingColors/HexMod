package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getNumOrVec
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.utils.Vector

object OpMulDot : ConstMediaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: Vector<Iota>, env: CastingEnvironment): Vector<Iota> {
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
