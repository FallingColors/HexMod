package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegister;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;

import java.util.*;
import java.util.function.Supplier;

public class HexDataComponents {
    private static final IXplatRegister<DataComponentType<?>> REGISTER = IXplatAbstractions.INSTANCE.createRegistar(Registries.DATA_COMPONENT_TYPE);

    public static void register() {
        REGISTER.registerAll();
    }

    public static final Supplier<DataComponentType<HexPattern>> PATTERN = REGISTER.register("pattern", () ->
            DataComponentType.<HexPattern>builder()
                    .persistent(HexPattern.CODEC)
                    .networkSynchronized(HexPattern.STREAM_CODEC)
                    .build());
    public static final Supplier<DataComponentType<ResourceKey<ActionRegistryEntry>>> ACTION = REGISTER.register("op_id", () ->
            DataComponentType.<ResourceKey<ActionRegistryEntry>>builder()
                    .persistent(ResourceKey.codec(HexRegistries.ACTION))
                    .networkSynchronized(ResourceKey.streamCodec(HexRegistries.ACTION))
                    .build());
    public static final Supplier<DataComponentType<ResourceKey<ActionRegistryEntry>>> RECALC_WARNING = REGISTER.register("recalc_warning", () ->
            DataComponentType.<ResourceKey<ActionRegistryEntry>>builder()
                    .persistent(ResourceKey.codec(HexRegistries.ACTION))
                    .networkSynchronized(ResourceKey.streamCodec(HexRegistries.ACTION))
                    .build());
    public static final Supplier<DataComponentType<Unit>> NEEDS_PURCHASE = REGISTER.register("needs_purchase", () ->
            DataComponentType.<Unit>builder()
                    .networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
                    .build());
    /**
     * If this datacomponent is set on the item, we ignore the rest of the item and render this as if it were of the
     * {@link at.petrak.hexcasting.api.casting.iota.IotaType IotaType} given by the resource location.
     * <p>
     * This is not useful to the player at all.
     */
    public static final Supplier<DataComponentType<Optional<IotaType<?>>>> VISUAL_OVERRIDE = REGISTER.register("visual_override", () ->
            DataComponentType.<Optional<IotaType<?>>>builder()
                    .networkSynchronized(ByteBufCodecs.optional(ByteBufCodecs.registry(HexRegistries.IOTA_TYPE)))
                    .build());
    public static final Supplier<DataComponentType<Integer>> ITEM_VARIANT = REGISTER.register("variant", () ->
            DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(0, Integer.MAX_VALUE))
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());
    public static final Supplier<DataComponentType<Unit>> SEALED_IOTA_HOLDER = REGISTER.register("sealed", () ->
            DataComponentType.<Unit>builder()
                    .persistent(Codec.unit(Unit.INSTANCE))
                    .networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
                    .build());
    // TODO port: Data components must implement equals and hashCode. Keep in mind they must also be immutable
    public static final Supplier<DataComponentType<Iota>> IOTA_HOLDER_IOTA = REGISTER.register("iota", () ->
            DataComponentType.<Iota>builder()
                    .persistent(IotaType.TYPED_CODEC)
                    .networkSynchronized(IotaType.TYPED_STREAM_CODEC)
                    .build());

    public static final Supplier<DataComponentType<List<Iota>>> HEX_HOLDER_PATTERNS = REGISTER.register("patterns", () ->
            DataComponentType.<List<Iota>>builder()
                    .persistent(IotaType.TYPED_CODEC.listOf())
                    .networkSynchronized(IotaType.TYPED_STREAM_CODEC.apply(ByteBufCodecs.list()))
                    .build());
    public static final Supplier<DataComponentType<Long>> MEDIA = REGISTER.register("media", () ->
            DataComponentType.<Long>builder()
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG)
                    .build());
    public static final Supplier<DataComponentType<Long>> MEDIA_MAX = REGISTER.register("start_media", () ->
            DataComponentType.<Long>builder()
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG)
                    .build());
    public static final Supplier<DataComponentType<String>> HEX_NAME = REGISTER.register("hex_name", () ->
            DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());

    public static final Supplier<DataComponentType<FrozenPigment>> PIGMENT = REGISTER.register("pigment", () ->
            DataComponentType.<FrozenPigment>builder()
                    .persistent(FrozenPigment.CODEC)
                    .networkSynchronized(FrozenPigment.STREAM_CODEC)
                    .build());

    public static final Supplier<DataComponentType<Double>> ABACUS_VALUE = REGISTER.register("abacus_value", () ->
            DataComponentType.<Double>builder()
                    .persistent(Codec.DOUBLE)
                    .networkSynchronized(ByteBufCodecs.DOUBLE)
                    .build());

    public static final Supplier<DataComponentType<Integer>> SELECTED_SPELLBOOK_PAGE = REGISTER.register("page_idx", () ->
            DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
                    .build());

    public static final Supplier<DataComponentType<Map<String, Iota>>> SPELLBOOK_PAGES = REGISTER.register("pages", () ->
            DataComponentType.<Map<String, Iota>>builder()
                    .persistent(Codec.unboundedMap(Codec.STRING, IotaType.TYPED_CODEC))
                    .networkSynchronized(ByteBufCodecs.map(
                            HashMap::newHashMap,
                            ByteBufCodecs.STRING_UTF8,
                            IotaType.TYPED_STREAM_CODEC
                    ))
                    .build());

    public static final Supplier<DataComponentType<Map<String, Component>>> SPELLBOOK_PAGE_NAMES = REGISTER.register("page_names", () ->
            DataComponentType.<Map<String, Component>>builder()
                    .persistent(Codec.unboundedMap(Codec.STRING, ComponentSerialization.CODEC))
                    .networkSynchronized(ByteBufCodecs.map(
                            HashMap::newHashMap,
                            ByteBufCodecs.STRING_UTF8,
                            ComponentSerialization.STREAM_CODEC
                    ))
                    .build());

    public static final Supplier<DataComponentType<Map<String, Boolean>>> SPELLBOOK_PAGE_SEALS = REGISTER.register("sealed_pages", () ->
            DataComponentType.<Map<String, Boolean>>builder()
                    .persistent(Codec.unboundedMap(Codec.STRING, Codec.BOOL))
                    .networkSynchronized(ByteBufCodecs.map(
                            HashMap::newHashMap,
                            ByteBufCodecs.STRING_UTF8,
                            ByteBufCodecs.BOOL
                    ))
                    .build());

    public static final Supplier<DataComponentType<List<Long>>> MEDIA_EXTRACTIONS = REGISTER.register("media_extractions", () ->
            DataComponentType.<List<Long>>builder()
                    .persistent(Codec.LONG.listOf())
                    .networkSynchronized(ByteBufCodecs.VAR_LONG.apply(ByteBufCodecs.list()))
                    .build());

    public static final Supplier<DataComponentType<List<Long>>> MEDIA_INSERTIONS = REGISTER.register("media_insertions", () ->
            DataComponentType.<List<Long>>builder()
                    .persistent(Codec.LONG.listOf())
                    .networkSynchronized(ByteBufCodecs.VAR_LONG.apply(ByteBufCodecs.list()))
                    .build());
}
