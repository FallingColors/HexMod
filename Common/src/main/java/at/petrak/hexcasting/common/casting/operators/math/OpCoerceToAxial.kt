package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3

object OpCoerceToAxial : ConstMediaAction {
    override val argc: Int
        get() = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val vec = args.getVec3(0, argc)
        if (vec == Vec3.ZERO)
            return vec.asActionResult
        return Vec3.atLowerCornerOf(Direction.getNearest(vec.x, vec.y, vec.z).normal).asActionResult
    }
}
