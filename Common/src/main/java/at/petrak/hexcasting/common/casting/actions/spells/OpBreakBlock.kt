package at.petrak.hexcasting.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

object OpBreakBlock : SpellAction {
    override val argc: Int
        get() = 1

    override fun execute(
        args: List<Iota>,
        ctx: CastingEnvironment
    ): SpellAction.Result {
        val vecPos = args.getVec3(0, argc)
        val pos = BlockPos.containing(vecPos)
        ctx.assertPosInRangeForEditing(pos)

        return SpellAction.Result(
            Spell(pos),
            MediaConstants.DUST_UNIT / 8,
            listOf(ParticleSpray.burst(Vec3.atCenterOf(pos), 1.0))
        )
    }

    private data class Spell(val pos: BlockPos) : RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            val blockstate = ctx.world.getBlockState(pos)
            val tier = HexConfig.server().opBreakHarvestLevel()

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
