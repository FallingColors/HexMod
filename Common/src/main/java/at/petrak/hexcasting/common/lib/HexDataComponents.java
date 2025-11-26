package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;

import java.util.*;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexDataComponents {
    public static void registerDataComponents(BiConsumer<DataComponentType<?>, ResourceLocation> r) {
        for (var e : DATA_COMPONENTS.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }

    private static final Map<ResourceLocation, DataComponentType<?>> DATA_COMPONENTS = new LinkedHashMap<>();

    public static final DataComponentType<HexPattern> PATTERN = register("pattern",
            DataComponentType.<HexPattern>builder()
                    .persistent(HexPattern.CODEC)
                    .networkSynchronized(HexPattern.STREAM_CODEC)
                    .build());
    public static final DataComponentType<ResourceKey<ActionRegistryEntry>> ACTION = register("op_id",
            DataComponentType.<ResourceKey<ActionRegistryEntry>>builder()
                    .persistent(ResourceKey.codec(HexRegistries.ACTION))
                    .networkSynchronized(ResourceKey.streamCodec(HexRegistries.ACTION))
                    .build());
    public static final DataComponentType<Unit> NEEDS_PURCHASE = register("needs_purchase",
            DataComponentType.<Unit>builder()
                    .networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
                    .build());
    /**
     * If this datacomponent is set on the item, we ignore the rest of the item and render this as if it were of the
     * {@link at.petrak.hexcasting.api.casting.iota.IotaType IotaType} given by the resource location.
     * <p>
     * This is not useful to the player at all.
     */
    public static final DataComponentType<Optional<IotaType<?>>> VISUAL_OVERRIDE = register("visual_override",
            DataComponentType.<Optional<IotaType<?>>>builder()
                    .networkSynchronized(ByteBufCodecs.optional(ByteBufCodecs.registry(HexRegistries.IOTA_TYPE)))
                    .build());
    public static final DataComponentType<Integer> VARIANT = register("variant",
            DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(0, Integer.MAX_VALUE))
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());
    public static final DataComponentType<Unit> SEALED = register("sealed",
            DataComponentType.<Unit>builder()
                    .persistent(Codec.unit(Unit.INSTANCE))
                    .networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
                    .build());
    // TODO port: Data components must implement equals and hashCode. Keep in mind they must also be immutable
    public static final DataComponentType<Iota> IOTA = register("iota",
            DataComponentType.<Iota>builder()
                    .persistent(IotaType.TYPED_CODEC)
                    .networkSynchronized(IotaType.TYPED_STREAM_CODEC)
                    .build());

    public static final DataComponentType<List<Iota>> PATTERNS = register("patterns",
            DataComponentType.<List<Iota>>builder()
                    .persistent(IotaType.TYPED_CODEC.listOf())
                    .networkSynchronized(IotaType.TYPED_STREAM_CODEC.apply(ByteBufCodecs.list()))
                    .build());
    public static final DataComponentType<Long> MEDIA = register("media",
            DataComponentType.<Long>builder()
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG)
                    .build());
    public static final DataComponentType<Long> MEDIA_MAX = register("start_media",
            DataComponentType.<Long>builder()
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG)
                    .build());
    public static final DataComponentType<String> HEX_NAME = register("hex_name",
            DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());

    public static final DataComponentType<FrozenPigment> PIGMENT = register("pigment",
            DataComponentType.<FrozenPigment>builder()
                    .persistent(FrozenPigment.CODEC)
                    .networkSynchronized(FrozenPigment.STREAM_CODEC)
                    .build());

    public static final DataComponentType<Double> ABACUS_VALUE = register("abacus_value",
            DataComponentType.<Double>builder()
                    .persistent(Codec.DOUBLE)
                    .networkSynchronized(ByteBufCodecs.DOUBLE)
                    .build());

    public static final DataComponentType<Integer> SELECTED_PAGE = register("page_idx",
            DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
                    .build());

    public static final DataComponentType<Map<String, Iota>> PAGES = register("pages",
            DataComponentType.<Map<String, Iota>>builder()
                    .persistent(Codec.unboundedMap(Codec.STRING, IotaType.TYPED_CODEC))
                    .networkSynchronized(ByteBufCodecs.map(
                            HashMap::newHashMap,
                            ByteBufCodecs.STRING_UTF8,
                            IotaType.TYPED_STREAM_CODEC
                    ))
                    .build());

    public static final DataComponentType<Map<String, Component>> PAGE_NAMES = register("page_names",
            DataComponentType.<Map<String, Component>>builder()
                    .persistent(Codec.unboundedMap(Codec.STRING, ComponentSerialization.CODEC))
                    .networkSynchronized(ByteBufCodecs.map(
                            HashMap::newHashMap,
                            ByteBufCodecs.STRING_UTF8,
                            ComponentSerialization.STREAM_CODEC
                    ))
                    .build());

    public static final DataComponentType<Map<String, Boolean>> PAGE_SEALS = register("sealed_pages",
            DataComponentType.<Map<String, Boolean>>builder()
                    .persistent(Codec.unboundedMap(Codec.STRING, Codec.BOOL))
                    .networkSynchronized(ByteBufCodecs.map(
                            HashMap::newHashMap,
                            ByteBufCodecs.STRING_UTF8,
                            ByteBufCodecs.BOOL
                    ))
                    .build());

    public static final DataComponentType<List<Long>> MEDIA_EXTRACTIONS = register("media_extractions",
            DataComponentType.<List<Long>>builder()
                    .persistent(Codec.LONG.listOf())
                    .networkSynchronized(ByteBufCodecs.VAR_LONG.apply(ByteBufCodecs.list()))
                    .build());

    public static final DataComponentType<List<Long>> MEDIA_INSERTIONS = register("media_insertions",
            DataComponentType.<List<Long>>builder()
                    .persistent(Codec.LONG.listOf())
                    .networkSynchronized(ByteBufCodecs.VAR_LONG.apply(ByteBufCodecs.list()))
                    .build());


    private static <T> DataComponentType<T> register(
            String id,
            DataComponentType<T> lift
    ) {
        var old = DATA_COMPONENTS.put(modLoc(id), lift);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + id);
        }
        return lift;
    }
}
