package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getNumOrVec
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.world.phys.Vec3

object OpSub : ConstManaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
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
