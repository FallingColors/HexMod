package at.petrak.hexcasting.common.casting.operators.spells.great

import at.petrak.hexcasting.HexMod
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.api.spell.casting.CastingContext
import net.minecraft.core.BlockPos
import net.minecraft.world.item.BucketItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.AbstractCauldronBlock
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.Vec3
import net.minecraftforge.fluids.FluidActionResult
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.FluidUtil

object OpCreateLava : SpellOperator {
    override val argc = 1
    override val isGreat = true
    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getChecked<Vec3>(0)
        ctx.assertVecInRange(target)

        return Triple(
            Spell(target),
            100_000,
            listOf(ParticleSpray.Burst(Vec3.atCenterOf(BlockPos(target)), 1.0)),
        )
    }

    private data class Spell(val target: Vec3) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val pos = BlockPos(target)

            if (!ctx.world.mayInteract(ctx.caster, pos))
                return

            val state = ctx.world.getBlockState(pos)

            if (state.block is AbstractCauldronBlock)
                ctx.world.setBlock(pos, Blocks.LAVA_CAULDRON.defaultBlockState(), 3)
            else if (FluidUtil.tryPlaceFluid(null, ctx.world, ctx.castingHand, pos, ItemStack(Items.LAVA_BUCKET), FluidStack(
                    Fluids.LAVA, 1000)) == FluidActionResult.FAILURE) {
                // Just steal bucket code lmao
                val charlie = Items.LAVA_BUCKET
                if (charlie is BucketItem) {
                    // make the player null so we don't give them a usage statistic for example
                    charlie.emptyContents(null, ctx.world, pos, null)
                } else {
                    HexMod.getLogger().warn("Items.WATER_BUCKET wasn't a BucketItem?")
                }
            }
        }
    }
}
