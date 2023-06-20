package at.petrak.hexcasting.common.lib.hex;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.iota.*;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Stores the registry for iota types, some utility methods, and all the types Hexcasting itself defines.
 */
@ParametersAreNonnullByDefault
public class HexIotaTypes {
    public static final Registry<IotaType<?>> REGISTRY = IXplatAbstractions.INSTANCE.getIotaTypeRegistry();
    public static final String
        KEY_TYPE = HexAPI.MOD_ID + ":type",
        KEY_DATA = HexAPI.MOD_ID + ":data";
    public static final int MAX_SERIALIZATION_DEPTH = 256;
    public static final int MAX_SERIALIZATION_TOTAL = 1024;

    public static void registerTypes(BiConsumer<IotaType<?>, ResourceLocation> r) {
        for (var e : TYPES.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }

    private static final Map<ResourceLocation, IotaType<?>> TYPES = new LinkedHashMap<>();

    public static final IotaType<NullIota> NULL = type("null", NullIota.TYPE);
    public static final IotaType<DoubleIota> DOUBLE = type("double", DoubleIota.TYPE);
    public static final IotaType<BooleanIota> BOOLEAN = type("boolean", BooleanIota.TYPE);
    public static final IotaType<EntityIota> ENTITY = type("entity", EntityIota.TYPE);
    public static final IotaType<ListIota> LIST = type("list", ListIota.TYPE);
    public static final IotaType<PatternIota> PATTERN = type("pattern", PatternIota.TYPE);
    public static final IotaType<GarbageIota> GARBAGE = type("garbage", GarbageIota.TYPE);
    public static final IotaType<Vec3Iota> VEC3 = type("vec3", Vec3Iota.TYPE);
    public static final IotaType<ContinuationIota> CONTINUATION = type("continuation", ContinuationIota.TYPE);


    private static <U extends Iota, T extends IotaType<U>> T type(String name, T type) {
        var old = TYPES.put(modLoc(name), type);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + name);
        }
        return type;
    }
}
