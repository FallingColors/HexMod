package at.petrak.hexcasting.fabric.interop.gravity

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import me.andrew.gravitychanger.api.GravityChangerAPI
import net.minecraft.core.Direction
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

object OpChangeGravity : SpellAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingContext):
            Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getEntity(0, argc)
        // TODO is it worth making a special "axial vector" getter
        val vec = args.getVec3(1, argc)

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