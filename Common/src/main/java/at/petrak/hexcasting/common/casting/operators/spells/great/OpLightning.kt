package at.petrak.hexcasting.common.casting.operators.spells.great

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getVec3
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LightningBolt
import net.minecraft.world.phys.Vec3

object OpLightning : SpellAction {
    override val argc = 1
    override val isGreat = true

    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getVec3(0, argc)
        ctx.assertVecInRange(target)
        return Triple(
            Spell(target),
            3 * ManaConstants.SHARD_UNIT,
            listOf(ParticleSpray(target.add(0.0, 2.0, 0.0), Vec3(0.0, -1.0, 0.0), 0.5, 0.1))
        )
    }

    private data class Spell(val target: Vec3) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            if (!ctx.world.mayInteract(ctx.caster, BlockPos(target)))
                return

            val lightning = LightningBolt(EntityType.LIGHTNING_BOLT, ctx.world)
            lightning.setPosRaw(target.x, target.y, target.z)
            ctx.world.addWithUUID(lightning) // why the hell is it called this it doesnt even involve a uuid
        }
    }
}
