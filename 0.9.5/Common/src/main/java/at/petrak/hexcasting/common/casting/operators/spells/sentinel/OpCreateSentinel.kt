package at.petrak.hexcasting.common.casting.operators.spells.sentinel

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.player.Sentinel

import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.phys.Vec3

class OpCreateSentinel(val extendsRange: Boolean) : SpellOperator {
    override val argc = 1
    override val isGreat = this.extendsRange

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getChecked<Vec3>(0, argc)
        ctx.assertVecInRange(target)

        return Triple(
            Spell(target, this.extendsRange),
            ManaConstants.DUST_UNIT * if (extendsRange) 2 else 1,
            listOf(ParticleSpray.burst(target, 2.0))
        )
    }

    private data class Spell(val target: Vec3, val extendsRange: Boolean) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            IXplatAbstractions.INSTANCE.setSentinel(
                ctx.caster,
                Sentinel(
                    true,
                    extendsRange,
                    target,
                    ctx.world.dimension()
                )
            )
        }
    }
}
