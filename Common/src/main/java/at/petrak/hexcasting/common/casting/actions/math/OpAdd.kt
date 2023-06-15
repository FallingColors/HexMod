package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getNumOrVec
import at.petrak.hexcasting.api.casting.iota.Iota

object OpAdd : ConstMediaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val lhs = args.getNumOrVec(0, argc)
        val rhs = args.getNumOrVec(1, argc)

        return lhs.map(
            { lnum ->
                rhs.map(
                    { rnum -> (lnum + rnum).asActionResult }, { rvec -> rvec.add(lnum, lnum, lnum).asActionResult }
                )
            },
            { lvec ->
                rhs.map(
                    { rnum -> lvec.add(rnum, rnum, rnum).asActionResult }, { rvec -> lvec.add(rvec).asActionResult }
                )
            }
        )
    }
}
