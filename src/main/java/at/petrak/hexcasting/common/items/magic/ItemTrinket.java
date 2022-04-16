package at.petrak.hexcasting.common.items.magic;

import net.minecraft.world.item.ItemStack;

public class ItemTrinket extends ItemPackagedSpell {
    public ItemTrinket(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean canDrawManaFromInventory(ItemStack stack) {
        return false;
    }

    @Override
    public boolean breakAfterDepletion() {
        return false;
    }
}
