package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.api.mod.HexConfig;
import net.minecraft.world.item.ItemStack;

public class ItemCypher extends ItemPackagedHex {
    public ItemCypher(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean canDrawMediaFromInventory(ItemStack stack) {
        return false;
    }

    @Override
    public boolean breakAfterDepletion() {
        return true;
    }

    @Override
    public int cooldown() {
        return HexConfig.common().cypherCooldown();
    }
}
