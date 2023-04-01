package at.petrak.hexcasting.common.casting.operators.spells.sentinel

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.player.Sentinel
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.phys.Vec3

class OpCreateSentinel(val extendsRange: Boolean) : SpellAction {
    override val argc = 1

    override fun execute(
        args: List<Iota>,
        ctx: CastingEnvironment
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getVec3(0, argc)
        ctx.assertVecInRange(target)

        return Triple(
            Spell(target, this.extendsRange),
            MediaConstants.DUST_UNIT * if (extendsRange) 2 else 1,
            listOf(ParticleSpray.burst(target, 2.0))
        )
    }

    private data class Spell(val target: Vec3, val extendsRange: Boolean) : RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            IXplatAbstractions.INSTANCE.setSentinel(
                ctx.caster,
                Sentinel(
                    extendsRange,
                    target,
                    ctx.world.dimension()
                )
            )
        }
    }
}
