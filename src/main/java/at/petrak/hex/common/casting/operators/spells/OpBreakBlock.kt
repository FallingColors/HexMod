package at.petrak.hex.common.casting.operators.spells

import at.petrak.hex.HexMod
import at.petrak.hex.api.Operator.Companion.getChecked
import at.petrak.hex.api.SpellOperator
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.RenderedSpell
import at.petrak.hex.common.casting.SpellDatum
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import net.minecraftforge.common.TierSortingRegistry

object OpBreakBlock : SpellOperator {
    override val argc: Int
        get() = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Pair<RenderedSpell, Int> {
        val pos = args.getChecked<Vec3>(0)
        ctx.assertVecInRange(pos)
        return Pair(
            Spell(pos),
            100_000
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
            } // TODO: else some kind of failureific particle effect?
        }
    }
}