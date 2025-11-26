package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
<<<<<<< HEAD
import com.mojang.datafixers.util.Pair;
=======
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
>>>>>>> refs/remotes/slava/devel/port-1.21
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

<<<<<<< HEAD
    /**
     * Get the color associated with this datum type.
     */
    public abstract int color();

    /**
     * Get the MapCodec associated with this datum type.
     */

    public abstract MapCodec<T> codec();

    /**
     * Get a display component that's the name of this iota type.
     */
    public Component typeName() {
        var key = HexIotaTypes.REGISTRY.getKey(this);
        return Component.translatable("hexcasting.iota." + key)
            .withStyle(style -> style.withColor(TextColor.fromRgb(color())));
    }

    public static CompoundTag serialize(Iota iota) {
        var type = iota.getType();
        var typeId = HexIotaTypes.REGISTRY.getKey(type);
        if (typeId == null) {
            throw new IllegalStateException(
                "Tried to serialize an unregistered iota type. Iota: " + iota
                    + " ; Type" + type.getClass().getTypeName());
        }

        // We check if it's too big on serialization; if it is we just return a garbage.
        if (isTooLargeToSerialize(List.of(iota), 0)) {
            // Garbage will never be too large so we just recurse
            return serialize(new GarbageIota());
        }
        var dataTag = iota.serialize();
        var out = new CompoundTag();
        out.putString(HexIotaTypes.KEY_TYPE, typeId.toString());
        out.put(HexIotaTypes.KEY_DATA, dataTag);
        return out;
    }
=======
    public abstract StreamCodec<RegistryFriendlyByteBuf, T> streamCodec();
>>>>>>> refs/remotes/slava/devel/port-1.21

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

<<<<<<< HEAD
    /**
     * This method attempts to find the type from the {@code type} key.
     * See {@link IotaType#serialize(Iota)} for the storage format.
     *
     * @return {@code null} if it cannot get the type.
     */
    @org.jetbrains.annotations.Nullable
    public static IotaType<?> getTypeFromTag(CompoundTag tag) {
        if (!tag.contains(HexIotaTypes.KEY_TYPE, Tag.TAG_STRING)) {
            return null;
        }
        var typeKey = tag.getString(HexIotaTypes.KEY_TYPE);
        var typeLoc = ResourceLocation.tryParse(typeKey);
        return HexIotaTypes.REGISTRY.get(typeLoc);
    }

    /**
     * Attempt to deserialize an iota from a tag.
     * <br>
     * Iotas are saved as such:
     * <code>
     * {
     * "type": "hexcasting:atype",
     * "data": {...}
     * }
     * </code>
     */
    public static Iota deserialize(CompoundTag tag, ServerLevel world) {
        var type = getTypeFromTag(tag);
        if (type == null) {
            return new GarbageIota();
        }
        var data = tag.get(HexIotaTypes.KEY_DATA);
        if (data == null) {
            return new GarbageIota();
        }
        Iota deserialized;
        try {
            deserialized = Objects.requireNonNullElse(type.deserialize(data, world), new NullIota());
        } catch (IllegalArgumentException exn) {
            HexAPI.LOGGER.warn("Caught an exception deserializing an iota", exn);
            deserialized = new GarbageIota();
        }
        return deserialized;
    }

    private static Component brokenIota() {
=======
    public static Component brokenIota() {
>>>>>>> refs/remotes/slava/devel/port-1.21
        return Component.translatable("hexcasting.spelldata.unknown")
            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    }

    public abstract int color();
}
