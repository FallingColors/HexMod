package at.petrak.hexcasting.fabric.interop.gravity

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import me.andrew.gravitychanger.api.GravityChangerAPI
import net.minecraft.core.Direction
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

object OpChangeGravity : SpellOperator {
    override val argc = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext):
            Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getChecked<Entity>(0)
        val vec = args.getChecked<Vec3>(1)

        val snapped = Direction.getNearest(vec.x, vec.y, vec.z)
        return Triple(
            Spell(target, snapped),
            100_000,
            listOf(ParticleSpray(target.position(), Vec3.atLowerCornerOf(snapped.normal), 0.1, 0.1))
        )
    }

    private data class Spell(val target: Entity, val dir: Direction) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            GravityChangerAPI.setDefaultGravityDirection(target, dir)
        }
    }
}