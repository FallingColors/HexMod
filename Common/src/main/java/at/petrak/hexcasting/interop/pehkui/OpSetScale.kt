package at.petrak.hexcasting.interop.pehkui

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getDoubleBetween
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.entity.Entity

object OpSetScale : SpellAction {
    override val argc = 2

    override fun execute(
            args: List<Iota>,
            env: CastingEnvironment
    ): SpellAction.Result {
        val target = args.getEntity(0, argc)
        val scale = args.getDoubleBetween(1, 1.0 / 32.0, 8.0, argc)

        return SpellAction.Result(
            Spell(target, scale),
            50_000,
            listOf(ParticleSpray.burst(target.position(), scale, 40))
        )
    }

    private data class Spell(val target: Entity, val scale: Double) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            IXplatAbstractions.INSTANCE.pehkuiApi.setScale(target, scale.toFloat())
        }
    }
}