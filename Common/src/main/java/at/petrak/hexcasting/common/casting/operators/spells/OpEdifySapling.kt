package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadBlock
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.common.misc.AkashicTreeGrower
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.tags.BlockTags
import net.minecraft.world.phys.Vec3

object OpEdifySapling : SpellAction {
    override val argc = 1

    override fun execute(
        args: List<Iota>,
        ctx: CastingEnvironment
    ): SpellAction.Result {
        val vecPos = args.getVec3(0, argc)
        val pos = BlockPos(vecPos)
        ctx.assertPosInRangeForEditing(pos)

        val bs = ctx.world.getBlockState(pos)
        if (!bs.`is`(BlockTags.SAPLINGS))
            throw MishapBadBlock.of(pos, "sapling")

        return SpellAction.Result(
            Spell(pos),
            MediaConstants.CRYSTAL_UNIT,
            listOf(ParticleSpray(Vec3.atCenterOf(pos), Vec3(0.0, 2.0, 0.0), 0.1, Math.PI / 4, 100))
        )
    }

    private data class Spell(val pos: BlockPos) : RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            val blockstate = ctx.world.getBlockState(pos)
            if (!ctx.canEditBlockAt(pos) ||
                !IXplatAbstractions.INSTANCE.isBreakingAllowed(ctx.world, pos, blockstate, ctx.caster)
            )
                return

            val bs = ctx.world.getBlockState(pos)
            for (i in 0 until 8) {
                val success = AkashicTreeGrower.INSTANCE.growTree(
                    ctx.world,
                    ctx.world.chunkSource.generator,
                    pos,
                    bs,
                    ctx.world.getRandom()
                )
                if (success) break
            }
        }
    }
}
