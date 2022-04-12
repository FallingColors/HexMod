package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.mod.HexApiItems
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.math.HexPattern
import net.minecraft.ChatFormatting
import net.minecraft.Util
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3

sealed class Mishap : Throwable() {
    /** Mishaps spray half-red, half-this-color. */
    abstract fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer

    open fun particleSpray(ctx: CastingContext): ParticleSpray {
        return ParticleSpray(ctx.position.add(0.0, 0.2, 0.0), Vec3(0.0, 2.0, 0.0), 0.2, Math.PI / 4, 40)
    }

    /**
     * Execute the actual effect, not any sfx.
     *
     * You can also mess up the stack with this.
     */
    abstract fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>)

    abstract fun errorMessage(ctx: CastingContext, errorCtx: Context): Component

    protected fun dyeColor(color: DyeColor): FrozenColorizer =
        FrozenColorizer(
            ItemStack(HexApiItems.getColorizer(color)),
            Util.NIL_UUID
        )

    protected fun error(stub: String, vararg args: Any): Component =
        TranslatableComponent("hexcasting.mishap.$stub", *args)

    protected fun actionName(action: ResourceLocation?): Component =
        TranslatableComponent("hexcasting.spell.${action ?: "unknown"}")
            .setStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE).withUnderlined(true))

    protected fun yeetItem(stack: ItemStack, ctx: CastingContext, delta: Vec3) {
        val entity = ItemEntity(
            ctx.world,
            ctx.position.x, ctx.position.y, ctx.position.z,
            stack,
            delta.x + (Math.random() - 0.5) * 0.1,
            delta.y + (Math.random() - 0.5) * 0.1,
            delta.z + (Math.random() - 0.5) * 0.1
        )
        entity.setPickUpDelay(40)
        ctx.world.addWithUUID(entity)
    }

    data class Context(val pattern: HexPattern, val action: ResourceLocation?)
}
