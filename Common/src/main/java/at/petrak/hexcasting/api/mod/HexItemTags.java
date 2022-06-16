package at.petrak.hexcasting.api.mod;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexItemTags {
    public static final TagKey<Item> EDIFIED_LOGS = create("edified_logs");
    public static final TagKey<Item> EDIFIED_PLANKS = create("edified_planks");
    public static final TagKey<Item> STAVES = create("staves");
    public static final TagKey<Item> PHIAL_BASE = create("phial_base");

    public static TagKey<Item> create(String name) {
        return create(modLoc(name));
    }

    public static TagKey<Item> create(ResourceLocation id) {
        return TagKey.create(Registry.ITEM_REGISTRY, id);
    }
}
