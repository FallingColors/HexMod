package at.petrak.hexcasting.common.casting.actions.eval

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.castables.SpecialHandler
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.FrameForEach
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getEvaluatable
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.math.HexAngle
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.math.HexSignature
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import at.petrak.hexcasting.api.utils.lightPurple
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import at.petrak.hexcasting.common.lib.hex.HexSpecialHandlers
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.network.chat.Component

class SpecialHandlerForEach(val n: Int) : SpecialHandler {
    override fun act(): Action {
        return InnerAction(n)
    }

    override fun getName(): Component {
        val key = IXplatAbstractions.INSTANCE.specialHandlerRegistry.getResourceKey(HexSpecialHandlers.FOR_EACH.get()).get()
        return HexAPI.instance().getSpecialHandlerI18nKey(key)
            .asTranslatedComponent(n.toString()).lightPurple
    }

    class InnerAction(val n: Int) : Action {
        override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
            var stack = image.stack

            if (stack.size < 2 + n)
                throw MishapNotEnoughArgs(2 + n, stack.size)

            val datums = stack.getList(stack.lastIndex - 1, stack.size)
            val instrs = stack.getEvaluatable(stack.lastIndex, stack.size)
            stack = stack.dropRight(2)

            val instrList = instrs.map({ TreeList.from(listOf(it)) }, { it })

            val contextStack = stack.takeRight(n)
            val stashedStack = stack.dropRight(n)

            val frame = FrameForEach(datums, instrList, contextStack, stashedStack, TreeList.empty())
            val image2 = image.withUsedOp().copy(stack = TreeList.empty())

            return OperationResult(image2, listOf(), continuation.pushFrame(frame), HexEvalSounds.THOTH.get())
        }
    }

    companion object {
        @JvmField
        val PREFIX: HexSignature = HexSignature.fromAnglesStringUnchecked("waaddw")
    }

    class Factory : SpecialHandler.Factory<SpecialHandlerForEach> {
        override fun tryMatch(pat: HexPattern, env: CastingEnvironment): SpecialHandlerForEach? {
            val sig = pat.signature
            val suffix = (sig.stripPrefix(PREFIX) ?: return null).asSequence()

            var tailLength = 0
            for ((index, segment) in suffix.chunked(2).withIndex()) {
                when (index % 2) {
                    0 -> if (segment != listOf(HexAngle.RIGHT_BACK, HexAngle.LEFT_BACK)) return null
                    1 -> if (segment != listOf(HexAngle.LEFT_BACK, HexAngle.RIGHT_BACK)) return null
                }
                tailLength++
            }

            return SpecialHandlerForEach(tailLength)
        }
    }
}
