package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.world.item.BucketItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.phys.Vec3

class OpCreateFluid(val cost: Int, val bucket: Item, val cauldron: BlockState, val fluid: Fluid) : SpellAction {
    override val argc = 1
    override fun execute(
        args: List<Iota>,
        ctx: CastingEnvironment
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getBlockPos(0, argc)
        ctx.assertVecInRange(target)

        return Triple(
            Spell(target, bucket, cauldron, fluid),
            cost,
            listOf(ParticleSpray.burst(Vec3.atCenterOf(BlockPos(target)), 1.0))
        )
    }

    private data class Spell(val pos: BlockPos, val bucket: Item, val cauldron: BlockState, val fluid: Fluid) : RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            if (!ctx.canEditBlockAt(pos) || !IXplatAbstractions.INSTANCE.isPlacingAllowed(
                    ctx.world,
                    pos,
                    ItemStack(bucket),
                    ctx.caster
                )
            )
                return

            val state = ctx.world.getBlockState(pos)

            if (state.block == Blocks.CAULDRON)
                ctx.world.setBlock(pos, cauldron, 3)
            else if (!IXplatAbstractions.INSTANCE.tryPlaceFluid(
                    ctx.world,
                    ctx.getCastingHand(),
                    pos,
                    fluid
                ) && bucket is BucketItem) {
                // make the player null so we don't give them a usage statistic for example
                bucket.emptyContents(null, ctx.world, pos, null)
            }
        }
    }
}
