package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.api.item.VariantItem;
import at.petrak.hexcasting.api.mod.HexConfig;
import net.minecraft.world.item.ItemStack;

import static at.petrak.hexcasting.common.items.storage.ItemFocus.NUM_VARIANTS;

public class ItemTrinket extends ItemPackagedHex implements VariantItem {
    public ItemTrinket(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean canDrawMediaFromInventory(ItemStack stack) {
        return false;
    }

    @Override
    public boolean breakAfterDepletion() {
        return false;
    }

    @Override
    public int cooldown() {
        return HexConfig.common().trinketCooldown();
    }

    @Override
    public int numVariants() {
        return NUM_VARIANTS;
    }
}
