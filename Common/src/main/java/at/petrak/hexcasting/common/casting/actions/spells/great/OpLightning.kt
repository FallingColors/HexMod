package at.petrak.hexcasting.common.casting.actions.spells.great

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadLocation
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LightningBolt
import net.minecraft.world.phys.Vec3

object OpLightning : SpellAction {
    override val argc = 1

    override fun execute(
            args: List<Iota>,
            env: CastingEnvironment
    ): SpellAction.Result {
        val target = args.getVec3(0, argc)
        env.assertVecInRange(target)

        if (!env.canEditBlockAt(BlockPos.containing(target)))
            throw MishapBadLocation(target, "forbidden")

        return SpellAction.Result(
            Spell(target),
            3 * MediaConstants.SHARD_UNIT,
            listOf(ParticleSpray(target.add(0.0, 2.0, 0.0), Vec3(0.0, -1.0, 0.0), 0.5, 0.1))
        )
    }

    private data class Spell(val target: Vec3) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {

            val lightning = LightningBolt(EntityType.LIGHTNING_BOLT, env.world)
            lightning.setPosRaw(target.x, target.y, target.z)
            env.world.addWithUUID(lightning) // why the hell is it called this it doesnt even involve a uuid
        }
    }
}
