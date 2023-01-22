package at.petrak.hexcasting.fabric.interop.gravity

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import com.fusionflux.gravity_api.api.GravityChangerAPI
import net.minecraft.core.Direction
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

object OpChangeGravity : SpellAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingEnvironment):
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
        override fun cast(ctx: CastingEnvironment) {
            GravityChangerAPI.setDefaultGravityDirection(target, dir)
        }
    }
}