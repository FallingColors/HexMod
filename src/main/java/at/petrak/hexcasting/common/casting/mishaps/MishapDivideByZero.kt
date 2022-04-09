package at.petrak.hexcasting.common.casting.mishaps

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.colors.FrozenColorizer
import at.petrak.hexcasting.common.lib.HexDamageSources
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.item.DyeColor
import net.minecraft.world.phys.Vec3

class MishapDivideByZero(val numerator: Component, val wasVec: Boolean) : Mishap() {
    constructor(number: Double) : this(TextComponent("%d".format(number)), false)
    constructor(vec: Vec3) : this(SpellDatum.make(vec).display(), true)

    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.RED)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        ctx.caster.hurt(HexDamageSources.OVERCAST, ctx.caster.health / 2)
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component {
        if (wasVec)
            return error("divide_by_zero_vec", numerator)
        return error("divide_by_zero", numerator)
    }
}
