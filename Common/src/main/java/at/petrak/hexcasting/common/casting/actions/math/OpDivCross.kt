package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getNumOrVec
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapDivideByZero
import net.minecraft.world.phys.Vec3

object OpDivCross : ConstMediaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val lhs = args.getNumOrVec(0, argc)
        val rhs = args.getNumOrVec(1, argc)
        val theMishap = MishapDivideByZero.of(args[0], args[1])

        return lhs.map(
            { lnum ->
                rhs.map(
                    { rnum ->
                        if (rnum == 0.0) throw theMishap // throw theMishap throw theMishap badumbadum
                        (lnum / rnum).asActionResult
                    },
                    { rvec ->
                        if (rvec.x == 0.0 || rvec.y == 0.0 || rvec.z == 0.0) throw theMishap
                        Vec3(lnum / rvec.x, lnum / rvec.y, lnum / rvec.z).asActionResult
                    }
                )
            }, { lvec ->
            rhs.map(
                { rnum ->
                    if (lvec == Vec3.ZERO) throw theMishap
                    lvec.scale(1.0 / rnum).asActionResult
                },
                { rvec -> lvec.cross(rvec).asActionResult }
            )
        })
    }
}
