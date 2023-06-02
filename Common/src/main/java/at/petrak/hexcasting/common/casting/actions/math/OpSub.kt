package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getNumOrVec
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.phys.Vec3

object OpSub : ConstMediaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val lhs = args.getNumOrVec(0, OpAdd.argc)
        val rhs = args.getNumOrVec(1, OpAdd.argc)

        return lhs.map({ lnum ->
            rhs.map(
                { rnum -> (lnum - rnum).asActionResult },
                { rvec -> Vec3(lnum - rvec.x, lnum - rvec.y, lnum - rvec.z).asActionResult }
            )
        }, { lvec ->
            rhs.map(
                { rnum -> lvec.subtract(rnum, rnum, rnum).asActionResult },
                { rvec -> lvec.subtract(rvec).asActionResult }
            )
        })
    }
}
