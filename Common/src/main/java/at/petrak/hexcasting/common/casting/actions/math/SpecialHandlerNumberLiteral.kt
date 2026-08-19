package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.castables.SpecialHandler
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.math.HexAngle
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.math.HexSignature
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import at.petrak.hexcasting.api.utils.lightPurple
import at.petrak.hexcasting.common.lib.hex.HexSpecialHandlers
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.network.chat.Component

class SpecialHandlerNumberLiteral(val x: Double) : SpecialHandler {
    override fun act(): Action {
        return InnerAction(this.x)
    }

    override fun getName(): Component {
        val key = IXplatAbstractions.INSTANCE.specialHandlerRegistry.getResourceKey(HexSpecialHandlers.NUMBER.get()).get()
        return HexAPI.instance().getSpecialHandlerI18nKey(key)
            .asTranslatedComponent(Action.DOUBLE_FORMATTER.format(x)).lightPurple
    }

    class InnerAction(val x: Double) : ConstMediaAction {
        override val argc = 0

        override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
            return this.x.asActionResult
        }
    }

    companion object {
        @JvmField
        val POSITIVE_PREFIX = HexSignature.fromAnglesStringUnchecked("aqaa")
        @JvmField
        val NEGATIVE_PREFIX = HexSignature.fromAnglesStringUnchecked("dedd")
    }

    class Factory : SpecialHandler.Factory<SpecialHandlerNumberLiteral> {
        override fun tryMatch(pat: HexPattern, env: CastingEnvironment): SpecialHandlerNumberLiteral? {
            val sig = pat.signature
            val (suffix, negate) = sig.stripPrefix(POSITIVE_PREFIX)?.let { it to false }
                ?: sig.stripPrefix(NEGATIVE_PREFIX)?.let { it to true }
                ?: return null

            var accumulator = 0.0;
            for (angle in suffix) {
                when (angle) {
                    HexAngle.FORWARD -> {
                        accumulator += 1;
                    }

                    HexAngle.LEFT -> {
                        accumulator += 5;
                    }

                    HexAngle.RIGHT -> {
                        accumulator += 10;
                    }

                    HexAngle.LEFT_BACK -> {
                        accumulator *= 2;
                    }

                    HexAngle.RIGHT_BACK -> {
                        accumulator /= 2;
                    }
                    // ok funny man
                    HexAngle.BACK -> {}
                    else -> throw IllegalStateException()
                }
            }
            if (negate) {
                accumulator = -accumulator;
            }
            return SpecialHandlerNumberLiteral(accumulator);
        }

    }
}