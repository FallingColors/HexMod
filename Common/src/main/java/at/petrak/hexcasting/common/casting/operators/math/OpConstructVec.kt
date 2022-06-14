package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getDouble
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.world.phys.Vec3

object OpConstructVec : ConstManaAction {
    override val argc = 3
    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val x = args.getDouble(0, argc)
        val y = args.getDouble(1, argc)
        val z = args.getDouble(2, argc)
        return Vec3(x, y, z).asActionResult
    }
}
