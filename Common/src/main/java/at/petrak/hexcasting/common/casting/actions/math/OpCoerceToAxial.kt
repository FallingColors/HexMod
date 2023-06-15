package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getNumOrVec
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3
import kotlin.math.sign

object OpCoerceToAxial : ConstMediaAction {
    override val argc: Int
        get() = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val value = args.getNumOrVec(0, argc)
        return value.map({ num ->
            num.sign.asActionResult
        }, { vec ->
            if (vec == Vec3.ZERO)
                vec.asActionResult
            else
                Vec3.atLowerCornerOf(Direction.getNearest(vec.x, vec.y, vec.z).normal).asActionResult
        })
    }
}
