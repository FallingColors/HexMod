package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.HexMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class HexItemTags {
    public static final TagKey<Item> AKASHIC_LOGS = create("akashic_logs");
    public static final TagKey<Item> AKASHIC_PLANKS = create("akashic_planks");
    public static final TagKey<Item> WANDS = create("wands");

    public static final TagKey<Item> AMETHYST_DUST = ItemTags.create(new ResourceLocation("forge", "dusts/amethyst"));

    private static TagKey<Item> create(String name) {
        return ItemTags.create(new ResourceLocation(HexMod.MOD_ID, name));
    }
}
