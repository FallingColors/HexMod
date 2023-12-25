package at.petrak.hexcasting.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.ktxt.UseOnContext
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3

object OpIgnite : SpellAction {
    override val argc = 1
    override fun execute(
            args: List<Iota>,
            env: CastingEnvironment
    ): SpellAction.Result {
        val target = args.getBlockPos(0, argc)
        env.assertPosInRangeForEditing(target)

        return SpellAction.Result(
            Spell(target),
            MediaConstants.DUST_UNIT,
            listOf(ParticleSpray.burst(Vec3.atCenterOf(BlockPos(target)), 1.0))
        )
    }

    private data class Spell(val pos: BlockPos) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            // help
            if (!tryToClick(env, pos, Items.FIRE_CHARGE)) {
                tryToClick(env, pos, Items.FLINT_AND_STEEL)
            }
        }

        fun tryToClick(ctx: CastingEnvironment, pos: BlockPos, item: Item): Boolean {
            return IXplatAbstractions.INSTANCE.isPlacingAllowed(ctx.world, pos, ItemStack(item), ctx.caster) &&
                item.useOn(
                    UseOnContext(
                        ctx.world,
                        null,
                        InteractionHand.MAIN_HAND,
                        ItemStack(item),
                        BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)
                    )
                ).consumesAction()
        }
    }
}
