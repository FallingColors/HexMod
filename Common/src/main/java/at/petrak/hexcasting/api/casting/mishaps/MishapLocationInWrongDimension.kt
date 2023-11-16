package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.DyeColor

class MishapLocationInWrongDimension(val properDimension: ResourceLocation) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.MAGENTA)

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        stack.add(GarbageIota())
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Component =
        error(
            "wrong_dimension", properDimension.toString(),
            ctx.world.dimension().location().toString()
        )
}
