package at.petrak.hex.common.casting.operators.spells

import at.petrak.hex.HexMod
import at.petrak.hex.api.SimpleOperator
import at.petrak.hex.api.SpellOperator.Companion.getChecked
import at.petrak.hex.api.SpellOperator.Companion.spellListOf
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.RenderedSpell
import at.petrak.hex.common.casting.RenderedSpellImpl
import at.petrak.hex.common.casting.SpellDatum
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import net.minecraftforge.common.TierSortingRegistry

object OpBreakBlock : SimpleOperator, RenderedSpellImpl {
    override val argc: Int
        get() = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Pair<List<SpellDatum<*>>, Int> {
        val pos = args.getChecked<Vec3>(0)
        ctx.assertVecInRange(pos)
        return Pair(
            spellListOf(RenderedSpell(OpBreakBlock, spellListOf(pos))),
            100
        )
    }

    override fun cast(args: List<SpellDatum<*>>, ctx: CastingContext) {
        val pos = BlockPos(args.getChecked<Vec3>(0))

        val blockstate = ctx.world.getBlockState(pos)
        val tier =
            HexMod.CONFIG.opBreakHarvestLevelBecauseForgeThoughtItWasAGoodIdeaToImplementHarvestTiersUsingAnHonestToGodTopoSort

        if (!blockstate.isAir && (!blockstate.requiresCorrectToolForDrops() || TierSortingRegistry.isCorrectTierForDrops(
                tier,
                blockstate
            ))
        ) {
            ctx.world.destroyBlock(pos, true, ctx.caster)
        } // TODO: else some kind of failureific particle effect?
    }
}