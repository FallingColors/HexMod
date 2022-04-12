package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.common.blocks.BlockConjured
import at.petrak.hexcasting.common.blocks.HexBlocks
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.player.HexPlayerDataHelper
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.DirectionalPlaceContext
import net.minecraft.world.phys.Vec3

class OpConjure(val light: Boolean) : SpellOperator {
    override val argc = 1
    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getChecked<Vec3>(0)
        ctx.assertVecInRange(target)

        return Triple(
            Spell(target, light),
            10_000,
            listOf(ParticleSpray.Cloud(Vec3.atCenterOf(BlockPos(target)), 1.0))
        )
    }

    private data class Spell(val target: Vec3, val light: Boolean) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val pos = BlockPos(target)
            val placeContext = DirectionalPlaceContext(ctx.world, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP)

            val worldState = ctx.world.getBlockState(pos)
            if (worldState.canBeReplaced(placeContext)) {
                val block = if (this.light) HexBlocks.CONJURED_LIGHT else HexBlocks.CONJURED_BLOCK
                val state = block.get().getStateForPlacement(placeContext)
                if (state != null) {
                    ctx.world.setBlock(pos, state, 2)

                    val colorizer = HexPlayerDataHelper.getColorizer(ctx.caster)

                    if (ctx.world.getBlockState(pos).block is BlockConjured) {
                        BlockConjured.setColor(ctx.world, pos, colorizer)
                    }
                }
            }
        }
    }
}
