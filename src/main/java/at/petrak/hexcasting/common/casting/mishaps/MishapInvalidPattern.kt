package at.petrak.hexcasting.common.casting.mishaps

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.colors.FrozenColorizer
import at.petrak.hexcasting.hexmath.HexPattern
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

class MishapInvalidPattern(val pattern: HexPattern) : Mishap() {
    override fun accentColor(ctx: CastingContext): FrozenColorizer =
        dyeColor(DyeColor.YELLOW)


    override fun execute(ctx: CastingContext, stack: MutableList<SpellDatum<*>>) {
        pushGarbage(stack)
    }

    override fun errorMessage(ctx: CastingContext): Component =
        error("invalid_pattern")
}