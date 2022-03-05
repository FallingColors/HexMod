package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.HexMod
import at.petrak.hexcasting.api.Operator.Companion.getChecked
import at.petrak.hexcasting.api.ParticleSpray
import at.petrak.hexcasting.api.RenderedSpell
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.api.SpellOperator
import at.petrak.hexcasting.common.casting.CastingContext
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import net.minecraftforge.common.TierSortingRegistry

object OpBreakBlock : SpellOperator {
    override val argc: Int
        get() = 1

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val pos = args.getChecked<Vec3>(0)
        ctx.assertVecInRange(pos)

        val centered = Vec3.atCenterOf(BlockPos(pos))
        return Triple(
            Spell(pos),
            20_000,
            listOf(ParticleSpray.Burst(centered, 1.0))
        )
    }

    private data class Spell(val v: Vec3) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val pos = BlockPos(v)

            val blockstate = ctx.world.getBlockState(pos)
            val tier =
                HexMod.CONFIG.opBreakHarvestLevelBecauseForgeThoughtItWasAGoodIdeaToImplementHarvestTiersUsingAnHonestToGodTopoSort

            if (
                !blockstate.isAir
                && blockstate.getDestroySpeed(ctx.world, pos) >= 0f // fix being able to break bedrock &c
                && (!blockstate.requiresCorrectToolForDrops()
                        || TierSortingRegistry.isCorrectTierForDrops(tier, blockstate))
            ) {
                ctx.world.destroyBlock(pos, true, ctx.caster)
            }
        }
    }
}