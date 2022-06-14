package at.petrak.hexcasting.fabric.interop.gravity

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getEntity
import at.petrak.hexcasting.api.spell.iota.Iota
import me.andrew.gravitychanger.api.GravityChangerAPI
import net.minecraft.world.phys.Vec3

object OpGetGravity : ConstManaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val target = args.getEntity(1)
        val grav = GravityChangerAPI.getGravityDirection(target)
        return Vec3.atLowerCornerOf(grav.normal).asActionResult
    }
}