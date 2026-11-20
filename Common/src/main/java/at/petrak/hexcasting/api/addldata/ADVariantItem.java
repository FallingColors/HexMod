package at.petrak.hexcasting.api.addldata;

import at.petrak.hexcasting.api.item.VariantItem;
import net.minecraft.world.item.ItemStack;

public record ADVariantItem(VariantItem variantItem, ItemStack stack) {
    public int numVariants() {
        return variantItem.numVariants();
    }

    public int getVariant() {
        return variantItem.getVariant(stack);
    }

    public void setVariant(int variant) {
        variantItem.setVariant(stack, variant);
    }
}
