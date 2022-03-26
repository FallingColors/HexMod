package at.petrak.hexcasting.common.casting.mishaps

import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.colors.FrozenColorizer
import at.petrak.hexcasting.common.items.HexItems
import net.minecraft.Util
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.DyeColor
import java.util.regex.Pattern

sealed class Mishap : Throwable() {
    /** Mishaps spray half-red, half-this-color. */
    abstract fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer

    open fun particleSpray(ctx: CastingContext): ParticleSpray {
        return ParticleSpray.Burst(ctx.position, 0.5)
    }

    /**
     * Execute the actual effect, not any sfx.
     *
     * You can also mess up the stack with this.
     */
    abstract fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>)

    abstract fun errorMessage(ctx: CastingContext, errorCtx: Context): Component

    protected fun dyeColor(color: DyeColor): FrozenColorizer =
        FrozenColorizer(HexItems.DYE_COLORIZERS[color]!!.get(), Util.NIL_UUID)

    protected fun error(stub: String, vararg args: Any): Component =
        TranslatableComponent("hexcasting.mishap.$stub", *args)

    protected fun actionName(action: ResourceLocation): Component =
        TranslatableComponent("hexcasting.spell.$action")

    data class Context(val pattern: Pattern, val action: ResourceLocation?)
}
