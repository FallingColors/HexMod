package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.api.mod.HexConfig;
import net.minecraft.world.item.ItemStack;

public class ItemArtifact extends ItemPackagedHex {
    public ItemArtifact(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean canDrawMediaFromInventory(ItemStack stack) {
        return true;
    }

    @Override
    public boolean breakAfterDepletion() {
        return false;
    }

    @Override
    public int cooldown() {
        return HexConfig.common().artifactCooldown();
    }
}
