package at.petrak.hexcasting.common.casting.operators.circles

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapNoSpellCircle
import net.minecraft.world.phys.Vec3

class OpCircleBounds(val max: Boolean) : ConstManaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        if (ctx.spellCircle == null)
            throw MishapNoSpellCircle()

        val aabb = ctx.spellCircle.aabb

        return if (max)
            Vec3(aabb.maxX - 0.5, aabb.maxY - 0.5, aabb.maxZ - 0.5).asActionResult
        else
            Vec3(aabb.minX + 0.5, aabb.minY + 0.5, aabb.minZ + 0.5).asActionResult
    }
}
