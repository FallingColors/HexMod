package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.casting.CastingContext
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
        error("location_$type", LegacySpellDatum.make(location).display(), actionName(errorCtx.action!!))
}
