package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.utils.HexDamageSources
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.world.item.DyeColor
import net.minecraft.world.phys.Vec3

class MishapDivideByZero(val operand1: Component, val operand2: Component, val suffix: String = "divide") : Mishap() {

    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.RED)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        stack.add(SpellDatum.make(Widget.GARBAGE))
        ctx.caster.hurt(HexDamageSources.OVERCAST, ctx.caster.health / 2)
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component {
        return error("divide_by_zero.$suffix", operand1, operand2)
    }

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
            return MishapDivideByZero(TranslatableComponent("$PREFIX.sin", translatedAngle), TranslatableComponent("$PREFIX.cos", translatedAngle))
        }

        @JvmStatic
        val zero get() = TranslatableComponent("$PREFIX.zero")
        @JvmStatic
        val zerothPower get() = TranslatableComponent("$PREFIX.zero.power")
        @JvmStatic
        val zeroVector get() = TranslatableComponent("$PREFIX.zero.vec")

        @JvmStatic
        fun powerOf(power: Component) = TranslatableComponent("$PREFIX.power", power)

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
