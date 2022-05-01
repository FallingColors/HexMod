package at.petrak.hexcasting.common.items.magic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ItemManaBattery extends ItemManaHolder {
    public static final ResourceLocation MANA_PREDICATE = modLoc("mana");
    public static final ResourceLocation MAX_MANA_PREDICATE = modLoc("max_mana");

    public ItemManaBattery(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean manaProvider(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canRecharge(ItemStack stack) {
        return true;
    }
}
