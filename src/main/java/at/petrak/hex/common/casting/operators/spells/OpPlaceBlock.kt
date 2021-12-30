package at.petrak.hex.common.casting.operators.spells

import at.petrak.hex.api.SimpleOperator
import at.petrak.hex.api.SpellOperator.Companion.getChecked
import at.petrak.hex.api.SpellOperator.Companion.spellListOf
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.RenderedSpell
import at.petrak.hex.common.casting.RenderedSpellImpl
import at.petrak.hex.common.casting.SpellDatum
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.util.BlockSnapshot
import net.minecraftforge.event.world.BlockEvent

object OpPlaceBlock : SimpleOperator, RenderedSpellImpl {
    override val argc: Int
        get() = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Pair<List<SpellDatum<*>>, Int> {
        val pos = args.getChecked<Vec3>(0)
        ctx.assertVecInRange(pos)
        return Pair(
            spellListOf(RenderedSpell(OpPlaceBlock, spellListOf(pos))),
            30
        )
    }

    override fun cast(args: List<SpellDatum<*>>, ctx: CastingContext) {
        val vec = args.getChecked<Vec3>(0)
        val pos = BlockPos(vec)
        val bstate = ctx.world.getBlockState(pos)
        if (bstate.isAir || bstate.material.isReplaceable) {
            val placeeSlot = ctx.getOperativeSlot { it.item is BlockItem }
            if (placeeSlot != null) {
                val placeeStack = ctx.caster.inventory.getItem(placeeSlot)
                val placee = placeeStack.item as BlockItem
                if (ctx.withdrawItem(placee, 1, false)) {
                    // https://github.com/VazkiiMods/Psi/blob/master/src/main/java/vazkii/psi/common/spell/trick/block/PieceTrickPlaceBlock.java#L143
                    val evt = BlockEvent.EntityPlaceEvent(
                        BlockSnapshot.create(ctx.world.dimension(), ctx.world, pos),
                        ctx.world.getBlockState(pos.above()),
                        ctx.caster
                    )
                    MinecraftForge.EVENT_BUS.post(evt)

                    // we temporarily give the player the stack, place it using mc code, then give them the old stack back.
                    val oldStack = ctx.caster.getItemInHand(ctx.wandHand)
                    val spoofedStack = placeeStack.copy()
                    spoofedStack.count = 1
                    ctx.caster.setItemInHand(ctx.wandHand, spoofedStack)

                    val blockHit = BlockHitResult(
                        Vec3.ZERO, ctx.caster.direction, pos, false
                    )
                    val itemUseCtx = UseOnContext(ctx.caster, ctx.wandHand, blockHit)
                    val res = spoofedStack.useOn(itemUseCtx)

                    ctx.caster.setItemInHand(ctx.wandHand, oldStack)
                    if (res != InteractionResult.FAIL) {
                        ctx.withdrawItem(placee, 1, true)
                    }
                }
            }
        }
    }
}