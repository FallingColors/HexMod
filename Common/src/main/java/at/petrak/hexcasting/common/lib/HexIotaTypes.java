package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.spell.iota.*;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Stores the registry for iota types, some utility methods, and all the types Hexcasting itself defines.
 */
public class HexIotaTypes {
    public static final Registry<IotaType<?>> REGISTRY = IXplatAbstractions.INSTANCE.getIotaTypeRegistry();
    public static final String
        KEY_TYPE = HexAPI.MOD_ID + ":type",
        KEY_DATA = HexAPI.MOD_ID + ":data";

    /**
     * This method attempts to find the type from the {@code type} key.
     * See {@link HexIotaTypes#getTypeFromTag} for the storage format.
     *
     * @return {@code null} if it cannot get the type.
     */
    @Nullable
    public static IotaType<?> getTypeFromTag(CompoundTag tag) {
        if (!tag.contains(KEY_TYPE, Tag.TAG_STRING)) {
            return null;
        }
        var typeKey = tag.getString(KEY_TYPE);
        if (!ResourceLocation.isValidResourceLocation(typeKey)) {
            return null;
        }
        var typeLoc = new ResourceLocation(typeKey);
        return REGISTRY.get(typeLoc);
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
    @Nullable
    public static Iota deserialize(CompoundTag tag, ServerLevel world) {
        var type = getTypeFromTag(tag);
        if (type == null) {
            return null;
        }
        var dataKey = tag.get(KEY_DATA);
        if (dataKey == null) {
            return null;
        }
        return type.deserialize(tag, world);
    }

    @ApiStatus.Internal
    public static void registerTypes() {
        BiConsumer<IotaType<?>, ResourceLocation> r = (type, id) -> Registry.register(REGISTRY, id, type);
        for (var e : TYPES.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }

    private static final Map<ResourceLocation, IotaType<?>> TYPES = new LinkedHashMap<>();

    public static final IotaType<NullIota> NULL = type("null", NullIota.TYPE);
    public static final IotaType<DoubleIota> DOUBLE = type("double", DoubleIota.TYPE);
    public static final IotaType<EntityIota> ENTITY = type("entity", EntityIota.TYPE);
    public static final IotaType<ListIota> LIST = type("list", ListIota.TYPE);
    public static final IotaType<PatternIota> PATTERN = type("pattern", PatternIota.TYPE);
    public static final IotaType<GarbageIota> GARBAGE = type("garbage", GarbageIota.TYPE);
    public static final IotaType<Vec3Iota> VEC3 = type("vec3", Vec3Iota.TYPE);


    private static <U extends Iota, T extends IotaType<U>> T type(String name, T type) {
        var old = TYPES.put(modLoc(name), type);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + name);
        }
        return type;
    }
}
