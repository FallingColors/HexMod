package at.petrak.hexcasting.common.lib.hex;

import at.petrak.hexcasting.api.casting.iota.*;
import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegistrar;
import net.minecraft.core.Registry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

/**
 * Stores the registry for iota types, some utility methods, and all the types Hexcasting itself defines.
 */
@ParametersAreNonnullByDefault
public class HexIotaTypes {
    private static final IXplatRegistrar<IotaType<?>> REGISTRAR = IXplatAbstractions.INSTANCE.createRegistrar(HexRegistries.IOTA_TYPE);

    public static final Registry<IotaType<?>> REGISTRY = IXplatAbstractions.INSTANCE.getIotaTypeRegistry();
    public static final int MAX_SERIALIZATION_DEPTH = 256;
    public static final int MAX_SERIALIZATION_TOTAL = 1024;

    public static void register() {
        REGISTRAR.registerAll();
    }

    public static final Supplier<IotaType<NullIota>> NULL = REGISTRAR.register("null", () -> NullIota.TYPE);
    public static final Supplier<IotaType<DoubleIota>> DOUBLE = REGISTRAR.register("double", () -> DoubleIota.TYPE);
    public static final Supplier<IotaType<BooleanIota>> BOOLEAN = REGISTRAR.register("boolean", () -> BooleanIota.TYPE);
    public static final Supplier<IotaType<EntityIota>> ENTITY = REGISTRAR.register("entity", () -> EntityIota.TYPE);
    public static final Supplier<IotaType<ListIota>> LIST = REGISTRAR.register("list", () -> ListIota.TYPE);
    public static final Supplier<IotaType<PatternIota>> PATTERN = REGISTRAR.register("pattern", () -> PatternIota.TYPE);
    public static final Supplier<IotaType<GarbageIota>> GARBAGE = REGISTRAR.register("garbage", () -> GarbageIota.TYPE);
    public static final Supplier<IotaType<Vec3Iota>> VEC3 = REGISTRAR.register("vec3", () -> Vec3Iota.TYPE);
    public static final Supplier<IotaType<ContinuationIota>> CONTINUATION = REGISTRAR.register("continuation", () -> ContinuationIota.TYPE);
}
