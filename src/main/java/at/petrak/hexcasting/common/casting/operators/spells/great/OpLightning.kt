package at.petrak.hexcasting.common.casting.operators.spells.great

import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.api.spell.casting.CastingContext
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LightningBolt
import net.minecraft.world.phys.Vec3

object OpLightning : SpellOperator {
    override val argc = 1
    override val isGreat = true

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getChecked<Vec3>(0)
        ctx.assertVecInRange(target)
        return Triple(
            Spell(target),
            150_000,
            listOf(ParticleSpray(target.add(0.0, 2.0, 0.0), Vec3(0.0, -1.0, 0.0), 0.5, 0.1))
        )
    }

    private data class Spell(val target: Vec3) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val lightning = LightningBolt(EntityType.LIGHTNING_BOLT, ctx.world)
            lightning.setPosRaw(target.x, target.y, target.z)
            ctx.world.addWithUUID(lightning) // why the hell is it called this it doesnt even involve a uuid
        }
    }
}
