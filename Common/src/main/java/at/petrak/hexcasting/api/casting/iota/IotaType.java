package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.function.Function;

// Take notes from ForgeRegistryEntry
public abstract class IotaType<T extends Iota> {

    /**
     * Get a codec associated with this datum. It can be used with {@code TYPED_CODEC} to serialize and deserialize said datum.
     * @return {@code MapCodec<T extends Iota>}
     */
    public abstract MapCodec<T> codec();

    /**
     * Get a {@link StreamCodec} associated with this datum.
     * Is used for Client <-> Server communication.
     * <p>
     * It can be used with {@code TYPED_STREAM_CODEC} to deserde said datum with {@link RegistryFriendlyByteBuf}
     * @return {@link StreamCodec} of {@link IotaType}
     */
    public abstract StreamCodec<RegistryFriendlyByteBuf, T> streamCodec();

    /**
     * Get the color associated with this datum type.
     */
    public abstract int color();

    public static final Codec<Iota> TYPED_CODEC = Codec.lazyInitialized(() -> IXplatAbstractions.INSTANCE
            .getIotaTypeRegistry()
            .byNameCodec()
            .<Iota>dispatch("type", Iota::getType, IotaType::codec)
            .comapFlatMap(
                    iota -> {
                        if (isTooLargeToSerialize(List.of(iota), 0)) {
                            return DataResult.success(new GarbageIota());
                        }
                        return DataResult.success(iota);
                    },
                    Function.identity()
            ).orElse(new GarbageIota())
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, Iota> TYPED_STREAM_CODEC = ByteBufCodecs
            .registry(HexRegistries.IOTA_TYPE)
            .dispatch(Iota::getType, IotaType::streamCodec);

    public boolean validate(T iota, ServerLevel level) {
        return true;
    }

    /**
     * Checks if an Iterable Iota object is too large for deserde.
     * @param examinee
     * @return {@code boolean}
     */
    public static boolean isTooLargeToSerialize(Iterable<Iota> examinee) {
        return isTooLargeToSerialize(examinee, 1);
    }

    private static boolean isTooLargeToSerialize(Iterable<Iota> examinee, int startingCount) {
        int totalSize = startingCount;
        for (Iota iota : examinee) {
            if (iota.depth() >= HexIotaTypes.MAX_SERIALIZATION_DEPTH)
                return true;
            totalSize += iota.size();
        }
        return totalSize >= HexIotaTypes.MAX_SERIALIZATION_TOTAL;
    }

    public static Component brokenIota() {
        return Component.translatable("hexcasting.spelldata.unknown")
            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    }
}
