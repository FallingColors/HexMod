package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.casting.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.phys.Vec3

object OpConstructVec : ConstMediaAction {
    override val argc = 3
    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val x = args.getDouble(0, argc)
        val y = args.getDouble(1, argc)
        val z = args.getDouble(2, argc)
        return Vec3(x, y, z).asActionResult
    }
}
