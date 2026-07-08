package at.petrak.hexcasting.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.common.recipe.CopyProperties
import at.petrak.hexcasting.common.recipe.HexRecipeStuffRegistry
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.Vec3

object OpFreeze : SpellAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val toFreeze = args.getBlockPos(0, argc)

        env.assertPosInRangeForEditing(toFreeze)

        return SpellAction.Result(
            Spell(toFreeze),
            MediaConstants.DUST_UNIT,
            listOf(ParticleSpray.burst(Vec3.atCenterOf(toFreeze), 1.0))
        )
    }

    private data class Spell(val pos: BlockPos) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val blockState = env.world.getBlockState(pos)

            if (!IXplatAbstractions.INSTANCE.isBreakingAllowed(env.world, pos, blockState, env.castingEntity as? ServerPlayer))
                return

            val recman = env.world.recipeManager
            val recipes = recman.getAllRecipesFor(HexRecipeStuffRegistry.FREEZE_TYPE).map{ holder -> holder.value }

            val recipe = recipes.find{ it.matches(blockState) }

            if (recipe != null)
                env.world.setBlockAndUpdate(pos, CopyProperties.copyProperties(blockState, recipe.result))
        }
    }
}