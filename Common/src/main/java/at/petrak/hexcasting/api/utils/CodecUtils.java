package at.petrak.hexcasting.api.utils;

import com.mojang.datafixers.util.Function7;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * Various helper functions for creating codecs. This is written in Java because writing codecs in Kotlin
 * causes some annoying warnings and type inference issues.
 */
public class CodecUtils {
    /**
     * Vanilla provides a Codec that works like this ({@link com.mojang.serialization.codecs.DispatchedMapCodec}),
     * but there's no equivalent StreamCodec, so we implement our own based on the mechanics of that class
     * plus the map-encoding system from {@link net.minecraft.network.codec.ByteBufCodecs#map}.
     */
    public static <B extends ByteBuf, K, V> StreamCodec<B, Map<K, V>> streamCodecDispatchedMap(
            IntFunction<Map<K, V>> mapCreator, StreamCodec<B, K> keyCodec,
            Function<K, StreamCodec<B, ? extends V>> valueCodecGetter
    ) {
        return new StreamCodec<>() {
            public Map<K, V> decode(B stream) {
                int i = ByteBufCodecs.readCount(stream, Integer.MAX_VALUE);
                Map<K, V> map = mapCreator.apply(Math.min(i, 65536));

                for (int j = 0; j < i; ++j) {
                    K key = keyCodec.decode(stream);
                    var valueCodec = valueCodecGetter.apply(key);
                    V value = valueCodec.decode(stream);
                    map.put(key, value);
                }

                return map;
            }

            public void encode(B stream, Map<K, V> map) {
                ByteBufCodecs.writeCount(stream, map.size(), Integer.MAX_VALUE);
                map.forEach((key, value) -> {
                    keyCodec.encode(stream, key);
                    var valueCodec = valueCodecGetter.apply(key);
                    // we can't just use .encode because valueCodec might actually be for a subclass of V
                    // so the value needs to be cast to that subclass before it can be properly encoded
                    encodeCasted(valueCodec, stream, value);
                });
            }

            @SuppressWarnings("unchecked")
            private <V2 extends V> void encodeCasted(StreamCodec<B, V2> codec, B stream, V value) {
                codec.encode(stream, (V2) value);
            }
        };
    }

    /**
     * Vanilla's {@link net.minecraft.network.codec.StreamCodec#composite} only supports up to six fields in 1.21,
     * so we implement our own version to handle things (specifically, the CastingImage) that need to encode seven fields.
     * @see at.petrak.hexcasting.api.casting.eval.vm.CastingImage
     */
    public static <B, C, T1, T2, T3, T4, T5, T6, T7> StreamCodec<B, C> compositeCodecSeven(
            StreamCodec<? super B, T1> streamCodec1, Function<C, T1> function1,
            StreamCodec<? super B, T2> streamCodec2, Function<C, T2> function2,
            StreamCodec<? super B, T3> streamCodec3, Function<C, T3> function3,
            StreamCodec<? super B, T4> streamCodec4, Function<C, T4> function4,
            StreamCodec<? super B, T5> streamCodec5, Function<C, T5> function5,
            StreamCodec<? super B, T6> streamCodec6, Function<C, T6> function6,
            StreamCodec<? super B, T7> streamCodec7, Function<C, T7> function7,
            Function7<T1, T2, T3, T4, T5, T6, T7, C> createFunction
    ) {
        return new StreamCodec<>() {
            public C decode(B stream) {
                T1 field1 = streamCodec1.decode(stream);
                T2 field2 = streamCodec2.decode(stream);
                T3 field3 = streamCodec3.decode(stream);
                T4 field4 = streamCodec4.decode(stream);
                T5 field5 = streamCodec5.decode(stream);
                T6 field6 = streamCodec6.decode(stream);
                T7 field7 = streamCodec7.decode(stream);
                return createFunction.apply(field1, field2, field3, field4, field5, field6, field7);
            }

            public void encode(B stream, C obj) {
                streamCodec1.encode(stream, function1.apply(obj));
                streamCodec2.encode(stream, function2.apply(obj));
                streamCodec3.encode(stream, function3.apply(obj));
                streamCodec4.encode(stream, function4.apply(obj));
                streamCodec5.encode(stream, function5.apply(obj));
                streamCodec6.encode(stream, function6.apply(obj));
                streamCodec7.encode(stream, function7.apply(obj));
            }
        };
    }
}
