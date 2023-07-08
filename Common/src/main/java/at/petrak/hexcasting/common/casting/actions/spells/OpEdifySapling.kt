package at.petrak.hexcasting.common.casting.actions.spells

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
            env: CastingEnvironment
    ): SpellAction.Result {
        val vecPos = args.getVec3(0, argc)
        val pos = BlockPos.containing(vecPos)
        env.assertPosInRangeForEditing(pos)

        val bs = env.world.getBlockState(pos)
        if (!bs.`is`(BlockTags.SAPLINGS))
            throw MishapBadBlock.of(pos, "sapling")

        return SpellAction.Result(
            Spell(pos),
            MediaConstants.CRYSTAL_UNIT,
            listOf(ParticleSpray(Vec3.atCenterOf(pos), Vec3(0.0, 2.0, 0.0), 0.1, Math.PI / 4, 100))
        )
    }

    private data class Spell(val pos: BlockPos) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val blockstate = env.world.getBlockState(pos)
            if (!env.canEditBlockAt(pos) ||
                !IXplatAbstractions.INSTANCE.isBreakingAllowed(env.world, pos, blockstate, env.caster)
            )
                return

            val bs = env.world.getBlockState(pos)
            for (i in 0 until 8) {
                val success = AkashicTreeGrower.INSTANCE.growTree(
                    env.world,
                    env.world.chunkSource.generator,
                    pos,
                    bs,
                    env.world.getRandom()
                )
                if (success) break
            }
        }
    }
}
