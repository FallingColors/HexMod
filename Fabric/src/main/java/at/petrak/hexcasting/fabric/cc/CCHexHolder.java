package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.addldata.HexHolder;
import at.petrak.hexcasting.api.item.HexHolderItem;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class CCHexHolder extends ItemComponent implements HexHolder {
    public CCHexHolder(ItemStack stack) {
        super(stack, HexCardinalComponents.HEX_HOLDER);
    }

    public static class ItemBased extends CCHexHolder {
        private final HexHolderItem spellHolder;

        public ItemBased(ItemStack owner) {
            super(owner);
            var item = owner.getItem();
            if (!(item instanceof HexHolderItem hexHolderItem)) {
                throw new IllegalStateException("item is not a colorizer: " + owner);
            }
            this.spellHolder = hexHolderItem;
        }


        @Override
        public boolean canDrawManaFromInventory() {
            return this.spellHolder.canDrawManaFromInventory(this.stack);
        }

        @Override
        public @Nullable List<HexPattern> getPatterns() {
            return this.spellHolder.getPatterns(this.stack);
        }

        @Override
        public void writePatterns(List<HexPattern> patterns, int mana) {
            this.spellHolder.writePatterns(this.stack, patterns, mana);
        }

        @Override
        public void clearPatterns() {
            this.spellHolder.clearPatterns(this.stack);
        }
    }
}
