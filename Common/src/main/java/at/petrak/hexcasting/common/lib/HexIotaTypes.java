package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.api.spell.datum.*;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexIotaTypes {
    public static final Registry<IotaType<?>> REGISTRY = IXplatAbstractions.INSTANCE.getIotaTypeRegistry();

    public static void registerTypes() {
        BiConsumer<IotaType<?>, ResourceLocation> r = (type, id) -> Registry.register(REGISTRY, id, type);
        for (var e : TYPES.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }

    private static final Map<ResourceLocation, IotaType<?>> TYPES = new LinkedHashMap<>();

    public static final IotaType<DatumNull> NULL = type("null", DatumNull.TYPE);
    public static final IotaType<DatumDouble> DOUBLE = type("double", DatumDouble.TYPE);
    public static final IotaType<DatumEntity> ENTITY = type("entity", DatumEntity.TYPE);

    private static <U extends Iota, T extends IotaType<U>> T type(String name, T type) {
        var old = TYPES.put(modLoc(name), type);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + name);
        }
        return type;
    }
}
