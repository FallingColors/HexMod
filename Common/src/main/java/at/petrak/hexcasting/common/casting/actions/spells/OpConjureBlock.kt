package at.petrak.hexcasting.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadBlock
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.common.blocks.BlockConjured
import at.petrak.hexcasting.common.lib.HexBlocks
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.DirectionalPlaceContext
import net.minecraft.world.phys.Vec3

class OpConjureBlock(val light: Boolean) : SpellAction {
    override val argc = 1
    override fun execute(
        args: List<Iota>,
        ctx: CastingEnvironment
    ): SpellAction.Result {
        val vecPos = args.getVec3(0, argc)
        val pos = BlockPos(vecPos)
        ctx.assertPosInRangeForEditing(pos)

        val placeContext = DirectionalPlaceContext(ctx.world, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP)

        val worldState = ctx.world.getBlockState(pos)
        if (!worldState.canBeReplaced(placeContext))
            throw MishapBadBlock.of(pos, "replaceable")

        return SpellAction.Result(
            Spell(pos, light),
            MediaConstants.DUST_UNIT,
            listOf(ParticleSpray.cloud(Vec3.atCenterOf(pos), 1.0))
        )
    }

    private data class Spell(val pos: BlockPos, val light: Boolean) : RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            if (!ctx.canEditBlockAt(pos))
                return

            val placeContext = DirectionalPlaceContext(ctx.world, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP)

            val worldState = ctx.world.getBlockState(pos)
            if (worldState.canBeReplaced(placeContext)) {
                val block = if (this.light) HexBlocks.CONJURED_LIGHT else HexBlocks.CONJURED_BLOCK

                if (ctx.caster != null && !IXplatAbstractions.INSTANCE.isPlacingAllowed(ctx.world, pos, ItemStack(block), ctx.caster))
                    return

                val state = block.getStateForPlacement(placeContext)
                if (state != null) {
                    ctx.world.setBlock(pos, state, 5)

                    val colorizer = ctx.colorizer

                    if (ctx.world.getBlockState(pos).block is BlockConjured) {
                        BlockConjured.setColor(ctx.world, pos, colorizer)
                    }
                }
            }
        }
    }
}
