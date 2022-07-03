package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getBlockPos
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.world.item.BucketItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.AbstractCauldronBlock
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LayeredCauldronBlock
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.Vec3

object OpCreateWater : SpellAction {
    override val argc = 1
    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getBlockPos(0, argc)

        return Triple(
            Spell(target),
            ManaConstants.DUST_UNIT,
            listOf(ParticleSpray.burst(Vec3.atCenterOf(target), 1.0))
        )
    }

    private data class Spell(val pos: BlockPos) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            if (!ctx.world.mayInteract(ctx.caster, pos) || !IXplatAbstractions.INSTANCE.isPlacingAllowed(ctx.world, pos, ItemStack(Items.WATER_BUCKET), ctx.caster))
                return
            val state = ctx.world.getBlockState(pos)

            if (state.block is AbstractCauldronBlock)
                ctx.world.setBlock(
                    pos,
                    Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3),
                    3
                )
            else if (!IXplatAbstractions.INSTANCE.tryPlaceFluid(
                    ctx.world,
                    ctx.castingHand,
                    pos,
                    ItemStack(Items.WATER_BUCKET),
                    Fluids.WATER
                )
            ) {
                // Just steal bucket code lmao
                val charlie = Items.WATER_BUCKET
                if (charlie is BucketItem) {
                    // make the player null so we don't give them a usage statistic for example
                    charlie.emptyContents(null, ctx.world, pos, null)
                } else {
                    HexAPI.LOGGER.warn("Items.WATER_BUCKET wasn't a BucketItem?")
                }
            }
        }
    }
}
