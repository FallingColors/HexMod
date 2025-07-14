package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.Vector
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor
import net.minecraft.world.phys.Vec3

class MishapBadLocation(val location: Vec3, val type: String = "too_far") : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.MAGENTA)

    override fun executeReturnStack(env: CastingEnvironment, errorCtx: Context, stack: Vector<Iota>): Vector<Iota> {
        env.mishapEnvironment.yeetHeldItemsTowards(this.location)
        return stack
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Component =
        error("location_$type", Vec3Iota.display(location))
}
