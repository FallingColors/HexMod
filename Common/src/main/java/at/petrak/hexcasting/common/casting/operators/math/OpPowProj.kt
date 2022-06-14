package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getNumOrVec
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapDivideByZero
import net.minecraft.world.phys.Vec3
import kotlin.math.pow

object OpPowProj : ConstManaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val lhs = args.getNumOrVec(0, OpAdd.argc)
        val rhs = args.getNumOrVec(1, OpAdd.argc)
        val theMishap = MishapDivideByZero.of(args[0], args[1], "exponent")

        return lhs.map(
            { lnum ->
                rhs.map(
                    { rnum ->
                        if (rnum == 0.0 && lnum == 0.0)
                            throw theMishap
                        lnum.pow(rnum).asActionResult
                    }, { rvec ->
                        if (lnum == 0.0 && (rvec.x == 0.0 || rvec.y == 0.0 || rvec.z == 0.0))
                            throw theMishap
                        Vec3(lnum.pow(rvec.x), lnum.pow(rvec.y), lnum.pow(rvec.z)).asActionResult
                    }
                )
            }, { lvec ->
                rhs.map(
                    { rnum ->
                        if (rnum == 0.0 && (lvec.x == 0.0 || lvec.y == 0.0 || lvec.z == 0.0))
                            throw theMishap
                        Vec3(lvec.x.pow(rnum), lvec.y.pow(rnum), lvec.z.pow(rnum)).asActionResult
                    },
                    { rvec ->
                        if (lvec == Vec3.ZERO)
                            throw MishapDivideByZero.of(args[0], args[1], "project")
                        rvec.scale(rvec.dot(lvec) / lvec.dot(lvec)).asActionResult
                    }
                )
            })
    }
}
