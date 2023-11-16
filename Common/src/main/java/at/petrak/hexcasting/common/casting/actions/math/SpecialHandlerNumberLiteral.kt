package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.castables.SpecialHandler
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.math.HexPattern
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
        val key = IXplatAbstractions.INSTANCE.specialHandlerRegistry.getResourceKey(HexSpecialHandlers.NUMBER).get()
        return HexAPI.instance().getSpecialHandlerI18nKey(key)
            .asTranslatedComponent(Action.DOUBLE_FORMATTER.format(x)).lightPurple
    }

    class InnerAction(val x: Double) : ConstMediaAction {
        override val argc = 0

        override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
            return this.x.asActionResult
        }
    }

    class Factory : SpecialHandler.Factory<SpecialHandlerNumberLiteral> {
        override fun tryMatch(pat: HexPattern): SpecialHandlerNumberLiteral? {
            val sig = pat.anglesSignature()
            if (sig.startsWith("aqaa") || sig.startsWith("dedd")) {
                val negate = sig.startsWith("dedd");
                var accumulator = 0.0;
                for (ch in sig.substring(4)) {
                    when (ch) {
                        'w' -> {
                            accumulator += 1;
                        }

                        'q' -> {
                            accumulator += 5;
                        }

                        'e' -> {
                            accumulator += 10;
                        }

                        'a' -> {
                            accumulator *= 2;
                        }

                        'd' -> {
                            accumulator /= 2;
                        }
                        // ok funny man
                        's' -> {}
                        else -> throw IllegalStateException()
                    }
                }
                if (negate) {
                    accumulator = -accumulator;
                }
                return SpecialHandlerNumberLiteral(accumulator);
            } else {
                return null;
            }
        }

    }
}