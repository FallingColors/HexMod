package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.misc.FrozenColorizer
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack

class MishapEntityTooFarAway(val entity: Entity) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.PINK)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        // Knock the player's items out of their hands
        val items = listOf(
            ctx.caster.mainHandItem.copy(),
            ctx.caster.offhandItem.copy()
        )
        for (hand in InteractionHand.values()) {
            ctx.caster.setItemInHand(hand, ItemStack.EMPTY.copy())
        }

        val delta = entity.position().subtract(ctx.position).normalize().scale(0.5)

        for (item in items) {
            yeetItem(item, ctx, delta)
        }
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component =
        error("entity_too_far", SpellDatum.make(entity).display(), actionName(errorCtx.action))
}
