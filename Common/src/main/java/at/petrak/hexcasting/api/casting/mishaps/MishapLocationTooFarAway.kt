package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.misc.FrozenColorizer
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor
import net.minecraft.world.phys.Vec3

class MishapLocationTooFarAway(val location: Vec3, val type: String = "too_far") : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.MAGENTA)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        yeetHeldItemsTowards(ctx, location)
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component =
        error("location_$type", Vec3Iota.display(location))
}
