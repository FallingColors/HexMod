package at.petrak.hexcasting.common.items.magic;

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
}
