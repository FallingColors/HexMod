package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.misc.HexDamageSources
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor
import net.minecraft.world.phys.Vec3

class MishapDivideByZero(val operand1: Component, val operand2: Component, val suffix: String = "divide") : Mishap() {

    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.RED)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        stack.add(SpellDatum.make(Widget.GARBAGE))
        trulyHurt(ctx.caster, HexDamageSources.OVERCAST, ctx.caster.health / 2)
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) =
        error("divide_by_zero.$suffix", operand1, operand2)

    companion object {
        private const val PREFIX = "hexcasting.mishap.divide_by_zero"

        @JvmStatic
        fun of(operand1: Any, operand2: Any, suffix: String = "divide"): MishapDivideByZero {
            if (suffix == "exponent")
                return MishapDivideByZero(translate(operand1), powerOf(operand2), suffix)
            return MishapDivideByZero(translate(operand1), translate(operand2), suffix)
        }

        @JvmStatic
        fun tan(angle: Double): MishapDivideByZero {
            val translatedAngle = translate(angle)
            return MishapDivideByZero(
                "$PREFIX.sin".asTranslatedComponent(translatedAngle),
                "$PREFIX.cos".asTranslatedComponent(translatedAngle)
            )
        }

        @JvmStatic
        val zero
            get() = "$PREFIX.zero".asTranslatedComponent

        @JvmStatic
        val zerothPower
            get() = "$PREFIX.zero.power".asTranslatedComponent

        @JvmStatic
        val zeroVector
            get() = "$PREFIX.zero.vec".asTranslatedComponent

        @JvmStatic
        fun powerOf(power: Component) = "$PREFIX.power".asTranslatedComponent(power)

        @JvmStatic
        fun powerOf(datum: Any) = when (datum) {
            0.0 -> zerothPower
            else -> powerOf(SpellDatum.make(datum).display())
        }

        @JvmStatic
        fun translate(datum: Any): Component = when (datum) {
            0.0 -> zero
            Vec3.ZERO -> zeroVector
            else -> SpellDatum.make(datum).display()
        }
    }
}
