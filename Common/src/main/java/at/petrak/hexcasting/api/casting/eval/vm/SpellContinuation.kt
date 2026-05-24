package at.petrak.hexcasting.api.casting.eval.vm

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import java.util.function.Function

/**
 * A continuation during the execution of a spell.
 */
sealed interface SpellContinuation {
    object Done : SpellContinuation

    data class NotDone(val frame: ContinuationFrame, val next: SpellContinuation) : SpellContinuation

    fun pushFrame(frame: ContinuationFrame): SpellContinuation = NotDone(frame, this)

    companion object {
        // TODO port: maybe serialize to list like before?
        // TODO port: maybe unit should be first
        @JvmStatic
        val CODEC = Codec.recursive<SpellContinuation>(
            SpellContinuation::class.java.simpleName
        ) { recursed: Codec<SpellContinuation> ->
            Codec.withAlternative<SpellContinuation>(
                Codec.unit(Done),
                RecordCodecBuilder.create<NotDone> { inst ->
                    inst.group(
                        ContinuationFrame.Type.TYPED_CODEC.fieldOf("frame").forGetter { it.frame },
                        recursed.fieldOf("next").forGetter { it.next }
                    ).apply(inst, ::NotDone)
                }
            )
        }
        @JvmStatic
        val STREAM_CODEC = StreamCodec.recursive<RegistryFriendlyByteBuf, SpellContinuation> { recursed ->
            withAlternative(
                StreamCodec.unit(Done),
                StreamCodec.composite(
                    ContinuationFrame.Type.TYPED_STREAM_CODEC, NotDone::frame,
                    recursed, NotDone::next,
                    ::NotDone
                ).map(Function.identity()) { it as NotDone }
            )
        }

        private fun <B: ByteBuf, T> withAlternative(primary: StreamCodec<B, T>, alternative: StreamCodec<B, T>): StreamCodec<B, T> {
            return ByteBufCodecs.either<B, T, T>(
                primary,
                alternative
            ).map<T>(
                Function { either: Either<T, T> -> Either.unwrap(either) },
                Function { value: T -> Either.left(value) }
            )
        }
    }
}
