package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getNumOrVec
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapDivideByZero
import net.minecraft.world.phys.Vec3

object OpDivCross : ConstManaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
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
