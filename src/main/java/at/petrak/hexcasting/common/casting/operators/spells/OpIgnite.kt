package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.HexMod
import at.petrak.hexcasting.api.Operator.Companion.getChecked
import at.petrak.hexcasting.api.RenderedSpell
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.api.SpellOperator
import at.petrak.hexcasting.common.casting.CastingContext
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.FireChargeItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3

object OpIgnite : SpellOperator {
    override val argc = 1
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<Vec3>> {
        val target = args.getChecked<Vec3>(0)
        ctx.assertVecInRange(target)

        return Triple(
            Spell(target),
            10_000,
            listOf(target)
        )
    }

    private data class Spell(val target: Vec3) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            // steal petra code that steals bucket code
            val maxwell = Items.FIRE_CHARGE
            if (maxwell is FireChargeItem) {
                // help
                maxwell.useOn(UseOnContext(ctx.world, null, InteractionHand.MAIN_HAND, ItemStack(maxwell.asItem()), BlockHitResult(target, Direction.UP, BlockPos(target), false)))
            } else {
                HexMod.getLogger().warn("Items.FIRE_CHARGE wasn't a FireChargeItem?")
            }
        }
    }
}