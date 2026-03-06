package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * On forge: these are setup in ForgeHexInit
 * On fabric: it's a mixin
 */
public class HexAttributes {
    public static void register(BiConsumer<Attribute, ResourceLocation> r) {
        for (var e : ATTRIBUTES.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }

    private static final Map<ResourceLocation, Attribute> ATTRIBUTES = new LinkedHashMap<>();

    private static final String MOD_ID = HexAPI.MOD_ID;

    // 1.21+ ResourceKeys for Holder lookup
    public static final ResourceKey<Attribute> GRID_ZOOM_KEY = ResourceKey.create(Registries.ATTRIBUTE, modLoc("grid_zoom"));
    public static final ResourceKey<Attribute> SCRY_SIGHT_KEY = ResourceKey.create(Registries.ATTRIBUTE, modLoc("scry_sight"));
    public static final ResourceKey<Attribute> FEEBLE_MIND_KEY = ResourceKey.create(Registries.ATTRIBUTE, modLoc("feeble_mind"));
    public static final ResourceKey<Attribute> MEDIA_CONSUMPTION_MODIFIER_KEY = ResourceKey.create(Registries.ATTRIBUTE, modLoc("media_consumption"));
    public static final ResourceKey<Attribute> AMBIT_RADIUS_KEY = ResourceKey.create(Registries.ATTRIBUTE, modLoc("ambit_radius"));
    public static final ResourceKey<Attribute> SENTINEL_RADIUS_KEY = ResourceKey.create(Registries.ATTRIBUTE, modLoc("sentinel_radius"));

    public static final Attribute GRID_ZOOM = make("grid_zoom", new RangedAttribute(
        MOD_ID + ".attributes.grid_zoom", 1.0, 0.5, 4.0)).setSyncable(true);

    /**
     * Whether you have the lens overlay when looking at something. 0 = no, > 0 = yes.
     */
    public static final Attribute SCRY_SIGHT = make("scry_sight", new RangedAttribute(
        MOD_ID + ".attributes.scry_sight", 0.0, 0.0, 1.0)).setSyncable(true);

    public static final Attribute FEEBLE_MIND = make("feeble_mind", new RangedAttribute(
            MOD_ID + ".attributes.feeble_mind", 0.0, 0.0, 1.0).setSyncable(true));

    public static final Attribute MEDIA_CONSUMPTION_MODIFIER = make("media_consumption", new RangedAttribute(
            MOD_ID + ".attributes.media_consumption", 1.0, 0.0, Double.MAX_VALUE).setSyncable(true));

    public static final Attribute AMBIT_RADIUS = make("ambit_radius", new RangedAttribute(
            MOD_ID + ".attributes.ambit_radius", PlayerBasedCastEnv.DEFAULT_AMBIT_RADIUS, 0.0, Double.MAX_VALUE).setSyncable(true));

    public static final Attribute SENTINEL_RADIUS = make("sentinel_radius", new RangedAttribute(
            MOD_ID + ".attributes.sentinel_radius", PlayerBasedCastEnv.DEFAULT_SENTINEL_RADIUS, 0.0, Double.MAX_VALUE).setSyncable(true));



    private static <T extends Attribute> T make(String id, T attr) {
        var old = ATTRIBUTES.put(modLoc(id), attr);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + id);
        }
        return attr;
    }

    /** 1.21+ Helper to get Holder for getAttributeValue */
    public static net.minecraft.core.Holder<Attribute> getHolder(Registry<Attribute> registry, ResourceKey<Attribute> key) {
        return registry.getHolderOrThrow(key);
    }

    public static net.minecraft.core.Holder<Attribute> getHolder(Entity entity, ResourceKey<Attribute> key) {
        return entity.registryAccess().registryOrThrow(Registries.ATTRIBUTE).getHolderOrThrow(key);
    }
}
