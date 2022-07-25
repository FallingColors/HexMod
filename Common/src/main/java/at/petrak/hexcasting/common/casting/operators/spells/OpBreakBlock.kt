package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

object OpBreakBlock : SpellOperator {
    override val argc: Int
        get() = 1

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val pos = args.getChecked<Vec3>(0, argc)
        ctx.assertVecInRange(pos)

        val centered = Vec3.atCenterOf(BlockPos(pos))
        return Triple(
            Spell(pos),
            (ManaConstants.DUST_UNIT * 1.125).toInt(),
            listOf(ParticleSpray.burst(centered, 1.0))
        )
    }

    private data class Spell(val v: Vec3) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val pos = BlockPos(v)

            if (!ctx.world.mayInteract(ctx.caster, pos))
                return

            val blockstate = ctx.world.getBlockState(pos)
            if (!IXplatAbstractions.INSTANCE.isBreakingAllowed(ctx.world, pos, blockstate, ctx.caster))
                return

            val tier =
                HexConfig.server().opBreakHarvestLevel()

            if (
                !blockstate.isAir
                && blockstate.getDestroySpeed(ctx.world, pos) >= 0f // fix being able to break bedrock &c
                && IXplatAbstractions.INSTANCE.isCorrectTierForDrops(tier, blockstate)
            ) {
                ctx.world.destroyBlock(pos, true, ctx.caster)
            }
        }
    }
}
