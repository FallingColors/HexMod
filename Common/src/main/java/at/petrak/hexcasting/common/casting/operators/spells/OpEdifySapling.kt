package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapBadBlock
import at.petrak.hexcasting.common.misc.AkashicTreeGrower
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.tags.BlockTags
import net.minecraft.world.phys.Vec3

object OpEdifySapling : SpellOperator {
    override val argc = 1

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val pos = args.getChecked<Vec3>(0, argc)
        ctx.assertVecInRange(pos)

        val bpos = BlockPos(pos)
        val bs = ctx.world.getBlockState(bpos)
        if (!bs.`is`(BlockTags.SAPLINGS))
            throw MishapBadBlock.of(bpos, "sapling")

        return Triple(
            Spell(bpos),
            ManaConstants.CRYSTAL_UNIT,
            listOf(ParticleSpray(Vec3.atCenterOf(bpos), Vec3(0.0, 2.0, 0.0), 0.1, Math.PI / 4, 100))
        )
    }

    private data class Spell(val pos: BlockPos) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
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
