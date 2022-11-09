package at.petrak.hexcasting.interop.pehkui

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.entity.Entity

object OpSetScale : SpellAction {
    override val argc = 2

    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getEntity(0, argc)
        val scale = args.getDoubleBetween(1, 1.0 / 32.0, 8.0, argc)

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