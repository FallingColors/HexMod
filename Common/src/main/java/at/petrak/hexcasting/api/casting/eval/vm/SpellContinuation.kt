package at.petrak.hexcasting.api.casting.eval.vm

import net.minecraft.network.codec.ByteBufCodecs

/**
 * A continuation during the execution of a spell.
 */
sealed interface SpellContinuation {
    object Done : SpellContinuation

    data class NotDone(val frame: ContinuationFrame, val next: SpellContinuation) : SpellContinuation

    fun pushFrame(frame: ContinuationFrame): SpellContinuation = NotDone(frame, this)

    companion object {
        @JvmStatic
        val CODEC = ContinuationFrame.Type.TYPED_CODEC.listOf().xmap(::fromList, ::toList)
        @JvmStatic
        val STREAM_CODEC = ContinuationFrame.Type.TYPED_STREAM_CODEC.apply(ByteBufCodecs.list()).map(::fromList, ::toList)

        private fun fromList(list: List<ContinuationFrame>): SpellContinuation {
            return list.foldRight(Done, ::NotDone)
        }

        private fun toList(sc: SpellContinuation): List<ContinuationFrame> {
            val result = ArrayList<ContinuationFrame>()
            var acc = sc
            while(acc is NotDone) {
                result.add(acc.frame)
                acc = acc.next
            }
            return result
        }
    }
}
