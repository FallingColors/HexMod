package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getBlockPos
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapBadBlock
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
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
        val pos = args.getBlockPos(0, argc)
        ctx.assertVecInRange(pos)

        if (!ctx.world.mayInteract(ctx.caster, pos))
            return null

        val placeContext = DirectionalPlaceContext(ctx.world, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP)

        val worldState = ctx.world.getBlockState(pos)
        if (!worldState.canBeReplaced(placeContext))
            throw MishapBadBlock.of(pos, "replaceable")

        return Triple(
            Spell(pos, light),
            ManaConstants.DUST_UNIT,
            listOf(ParticleSpray.cloud(Vec3.atCenterOf(pos), 1.0))
        )
    }

    private data class Spell(val pos: BlockPos, val light: Boolean) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            if (!ctx.world.mayInteract(ctx.caster, pos))
                return

            val placeContext = DirectionalPlaceContext(ctx.world, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP)

            val worldState = ctx.world.getBlockState(pos)
            if (worldState.canBeReplaced(placeContext)) {
                val block = if (this.light) HexBlocks.CONJURED_LIGHT else HexBlocks.CONJURED_BLOCK

                if (!IXplatAbstractions.INSTANCE.isPlacingAllowed(ctx.world, pos, ItemStack(block), ctx.caster))
                    return

                val state = block.getStateForPlacement(placeContext)
                if (state != null) {
                    ctx.world.setBlock(pos, state, 5)

                    val colorizer = IXplatAbstractions.INSTANCE.getColorizer(ctx.caster)

                    if (ctx.world.getBlockState(pos).block is BlockConjured) {
                        BlockConjured.setColor(ctx.world, pos, colorizer)
                    }
                }
            }
        }
    }
}
