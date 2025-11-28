package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.function.Function;

// Take notes from ForgeRegistryEntry
public abstract class IotaType<T extends Iota> {
    public static final Codec<Iota> TYPED_CODEC = Codec.lazyInitialized(() -> IXplatAbstractions.INSTANCE
            .getIotaTypeRegistry()
            .byNameCodec()
            .<Iota>dispatch("type", Iota::getType, IotaType::codec)
            .comapFlatMap(
                    iota -> {
                        if (isTooLargeToSerialize(List.of(iota), 0)) {
                            return DataResult.success(GarbageIota.INSTANCE);
                        }
                        return DataResult.success(iota);
                    },
                    Function.identity()
            ).orElse(GarbageIota.INSTANCE)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, Iota> TYPED_STREAM_CODEC = ByteBufCodecs
            .registry(HexRegistries.IOTA_TYPE)
            .dispatch(Iota::getType, IotaType::streamCodec);


    public abstract MapCodec<T> codec();

    public abstract StreamCodec<RegistryFriendlyByteBuf, T> streamCodec();

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

    public abstract int color();
}
