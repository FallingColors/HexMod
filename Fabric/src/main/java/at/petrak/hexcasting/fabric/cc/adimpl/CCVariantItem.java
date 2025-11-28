package at.petrak.hexcasting.fabric.cc.adimpl;

import at.petrak.hexcasting.api.addldata.ADVariantItem;
import at.petrak.hexcasting.api.item.VariantItem;
import at.petrak.hexcasting.fabric.cc.HexCardinalComponents;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import org.ladysnake.cca.api.v3.component.Component;
import net.minecraft.world.item.ItemStack;

public abstract class CCVariantItem implements ADVariantItem, Component {
    final ItemStack stack;
    public CCVariantItem(ItemStack stack) {
        this.stack = stack;
    }

    public static class ItemBased extends CCVariantItem {
        private final VariantItem variantItem;

        public ItemBased(ItemStack owner) {
            super(owner);
            var item = owner.getItem();
            if (!(item instanceof VariantItem variantItem)) {
                throw new IllegalStateException("item is not a colorizer: " + owner);
            }
            this.variantItem = variantItem;
        }

        @Override
        public int numVariants() {
            return variantItem.numVariants();
        }

        @Override
        public int getVariant() {
            return variantItem.getVariant(this.stack);
        }

        @Override
        public void setVariant(int variant) {
            variantItem.setVariant(this.stack, variant);
        }

        @Override
        public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {

        }

        @Override
        public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {

        }
    }
}
