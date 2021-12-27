package at.petrak.hex.common.items;

import at.petrak.hex.HexMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ItemFocus extends Item {
    public static final ResourceLocation PREDICATE = new ResourceLocation(HexMod.MOD_ID, "datatype");

    public ItemFocus(Properties pProperties) {
        super(pProperties);
    }
}
