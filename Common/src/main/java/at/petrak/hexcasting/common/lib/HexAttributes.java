package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.api.HexAPI;
import net.minecraft.resources.ResourceLocation;
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

    public static final Attribute GRID_ZOOM = make("grid_zoom", new RangedAttribute(
        HexAPI.MOD_ID + ".attributes.grid_zoom", 1.0, 0.5, 4.0)).setSyncable(true);

    /**
     * Whether you have the lens overlay when looking at something. 0 = no, > 0 = yes.
     */
    public static final Attribute SCRY_SIGHT = make("scry_sight", new RangedAttribute(
        HexAPI.MOD_ID + ".attributes.scry_sight", 0.0, 0.0, 1.0)).setSyncable(true);

    private static <T extends Attribute> T make(String id, T attr) {
        var old = ATTRIBUTES.put(modLoc(id), attr);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + id);
        }
        return attr;
    }
}
