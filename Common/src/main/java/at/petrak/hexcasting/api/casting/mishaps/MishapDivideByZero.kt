package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor
import net.minecraft.world.phys.Vec3

class MishapDivideByZero(val operand1: Component, val operand2: Component, val suffix: String = "divide") : Mishap() {

    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.RED)

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        stack.add(GarbageIota())
        env.mishapEnvironment.damage(0.5f)
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) =
        error("divide_by_zero.$suffix", operand1, operand2)

    companion object {
        private const val PREFIX = "hexcasting.mishap.divide_by_zero"

        @JvmStatic
        fun of(operand1: Double, operand2: Double, suffix: String = "divide"): MishapDivideByZero {
            if (suffix == "exponent")
                return MishapDivideByZero(translate(DoubleIota(operand1)), powerOf(DoubleIota(operand2)), suffix)
            return MishapDivideByZero(translate(DoubleIota(operand1)), translate(DoubleIota(operand2)), suffix)
        }

        @JvmStatic
        fun of(operand1: Iota, operand2: Iota, suffix: String = "divide"): MishapDivideByZero {
            if (suffix == "exponent")
                return MishapDivideByZero(translate(operand1), powerOf(operand2), suffix)
            return MishapDivideByZero(translate(operand1), translate(operand2), suffix)
        }

        @JvmStatic
        fun tan(angle: Double): MishapDivideByZero {
            val translatedAngle = translate(DoubleIota(angle))
            return MishapDivideByZero(
                    "$PREFIX.sin".asTranslatedComponent(translatedAngle),
                    "$PREFIX.cos".asTranslatedComponent(translatedAngle)
            )
        }

        @JvmStatic
        fun tan(angle: DoubleIota): MishapDivideByZero {
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
        fun powerOf(datum: Iota): Component = when {
            datum is DoubleIota && datum.double == 0.0 -> zerothPower
            else -> datum.display()
        }

        @JvmStatic
        fun translate(datum: Iota): Component = when {
            datum is DoubleIota && datum.double == 0.0 -> zero
            datum is Vec3Iota && datum.vec3 == Vec3.ZERO -> zeroVector
            else -> datum.display()
        }
    }
}
