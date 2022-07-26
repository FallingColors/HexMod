package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.ktxt.UseOnContext
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.FireChargeItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3

object OpIgnite : SpellOperator {
    override val argc = 1
    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getChecked<Vec3>(0, argc)
        ctx.assertVecInRange(target)

        return Triple(
            Spell(target),
            ManaConstants.DUST_UNIT,
            listOf(ParticleSpray.burst(Vec3.atCenterOf(BlockPos(target)), 1.0))
        )
    }

    private data class Spell(val target: Vec3) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val pos = BlockPos(target)

            // steal petra code that steals bucket code
            val maxwell = Items.FIRE_CHARGE

            if (!ctx.canEditBlockAt(pos) || !IXplatAbstractions.INSTANCE.isPlacingAllowed(
                    ctx.world,
                    pos,
                    ItemStack(maxwell),
                    ctx.caster
                )
            )
                return

            if (maxwell is FireChargeItem) {
                // help
                maxwell.useOn(
                    UseOnContext(
                        ctx.world,
                        null,
                        InteractionHand.MAIN_HAND,
                        ItemStack(maxwell),
                        BlockHitResult(target, Direction.UP, pos, false)
                    )
                )
            } else {
                HexAPI.LOGGER.warn("Items.FIRE_CHARGE wasn't a FireChargeItem?")
            }
        }
    }
}
