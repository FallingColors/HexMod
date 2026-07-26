package at.petrak.hexcasting.common.lib.hex;

import at.petrak.hexcasting.api.casting.iota.*;
import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegister;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Stores the registry for iota types, some utility methods, and all the types Hexcasting itself defines.
 */
@ParametersAreNonnullByDefault
public class HexIotaTypes {
    private static final IXplatRegister<IotaType<?>> REGISTER = IXplatAbstractions.INSTANCE.createRegistar(HexRegistries.IOTA_TYPE);

    public static final Registry<IotaType<?>> REGISTRY = IXplatAbstractions.INSTANCE.getIotaTypeRegistry();
    public static final int MAX_SERIALIZATION_DEPTH = 256;
    public static final int MAX_SERIALIZATION_TOTAL = 1024;

    public static void register() {
        REGISTER.registerAll();
    }

    private static final Map<ResourceLocation, IotaType<?>> TYPES = new LinkedHashMap<>();

    public static final Supplier<IotaType<NullIota>> NULL = REGISTER.register("null", () -> NullIota.TYPE);
    public static final Supplier<IotaType<DoubleIota>> DOUBLE = REGISTER.register("double", () -> DoubleIota.TYPE);
    public static final Supplier<IotaType<BooleanIota>> BOOLEAN = REGISTER.register("boolean", () -> BooleanIota.TYPE);
    public static final Supplier<IotaType<EntityIota>> ENTITY = REGISTER.register("entity", () -> EntityIota.TYPE);
    public static final Supplier<IotaType<ListIota>> LIST = REGISTER.register("list", () -> ListIota.TYPE);
    public static final Supplier<IotaType<PatternIota>> PATTERN = REGISTER.register("pattern", () -> PatternIota.TYPE);
    public static final Supplier<IotaType<GarbageIota>> GARBAGE = REGISTER.register("garbage", () -> GarbageIota.TYPE);
    public static final Supplier<IotaType<Vec3Iota>> VEC3 = REGISTER.register("vec3", () -> Vec3Iota.TYPE);
    public static final Supplier<IotaType<ContinuationIota>> CONTINUATION = REGISTER.register("continuation", () -> ContinuationIota.TYPE);
}
