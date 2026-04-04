package at.petrak.hexcasting.common.casting.actions.queryentity

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.entity.monster.Phantom
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.entity.projectile.ShulkerBullet
import net.minecraft.world.phys.Vec3

object OpEntityLook : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val e = args.getEntity(0, argc)
        env.assertEntityInRange(e)

        var lookDir = e.lookAngle
        if (e is Projectile) { // https://bugs.mojang.com/browse/MC/issues/MC-112474
            if (e is AbstractHurtingProjectile || e is ShulkerBullet)
                lookDir = Vec3(-1 * lookDir.x, lookDir.y, -1 * lookDir.z)
            else
                lookDir = Vec3(-1 * lookDir.x, -1 * lookDir.y, lookDir.z)
        } else if (e is Phantom) { // https://bugs.mojang.com/browse/MC/issues/MC-134707
            lookDir = Vec3(lookDir.x, -1 * lookDir.y, lookDir.z)
        }

        return lookDir.asActionResult
    }
}
