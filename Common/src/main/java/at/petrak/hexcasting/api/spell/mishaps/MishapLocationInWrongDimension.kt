package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.misc.FrozenColorizer
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.DyeColor

class MishapLocationInWrongDimension(val properDimension: ResourceLocation) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.MAGENTA)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        stack.add(SpellDatum.make(Widget.GARBAGE))
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component =
        error("wrong_dimension", actionName(errorCtx.action!!), properDimension.toString(),
            ctx.world.dimension().location().toString())
}
