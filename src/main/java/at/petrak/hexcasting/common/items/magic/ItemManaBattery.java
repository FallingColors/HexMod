package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.HexMod;
import net.minecraft.resources.ResourceLocation;

public class ItemManaBattery extends ItemManaHolder {
    public static final ResourceLocation MANA_PREDICATE = new ResourceLocation(HexMod.MOD_ID, "mana");
    public static final ResourceLocation MAX_MANA_PREDICATE = new ResourceLocation(HexMod.MOD_ID, "max_mana");

    public ItemManaBattery(Properties pProperties) {
        super(pProperties);
    }
}
