package at.petrak.hexcasting.interop.pehkui

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity

object OpSetScale : SpellOperator {
    override val argc = 2

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getChecked<Entity>(0)
        val scale = Mth.clamp(args.getChecked<Double>(1), 1.0 / 32.0, 8.0)

        return Triple(
            Spell(target, scale),
            50_000,
            listOf(ParticleSpray.burst(target.position(), scale, 40))
        )
    }

    private data class Spell(val target: Entity, val scale: Double) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            IXplatAbstractions.INSTANCE.pehkuiApi.setScale(target, scale.toFloat())
        }
    }
}