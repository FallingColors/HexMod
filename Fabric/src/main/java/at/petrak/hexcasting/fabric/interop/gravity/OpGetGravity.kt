package at.petrak.hexcasting.fabric.interop.gravity

import at.petrak.hexcasting.api.casting.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import com.fusionflux.gravity_api.api.GravityChangerAPI
import net.minecraft.world.phys.Vec3

object OpGetGravity : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val target = args.getEntity(0)
        val grav = GravityChangerAPI.getGravityDirection(target)
        return Vec3.atLowerCornerOf(grav.normal).asActionResult
    }
}
