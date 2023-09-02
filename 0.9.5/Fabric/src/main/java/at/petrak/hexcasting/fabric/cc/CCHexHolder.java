package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.addldata.HexHolder;
import at.petrak.hexcasting.api.item.HexHolderItem;
import at.petrak.hexcasting.api.spell.SpellDatum;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class CCHexHolder extends ItemComponent implements HexHolder {
    public CCHexHolder(ItemStack stack) {
        super(stack, HexCardinalComponents.HEX_HOLDER);
    }

    public static class ItemBased extends CCHexHolder {
        private final HexHolderItem hexHolder;

        public ItemBased(ItemStack owner) {
            super(owner);
            var item = owner.getItem();
            if (!(item instanceof HexHolderItem hexHolderItem)) {
                throw new IllegalStateException("item is not a colorizer: " + owner);
            }
            this.hexHolder = hexHolderItem;
        }


        @Override
        public boolean canDrawManaFromInventory() {
            return this.hexHolder.canDrawManaFromInventory(this.stack);
        }

        @Override
        public boolean hasHex() {
            return this.hexHolder.hasHex(this.stack);
        }

        @Override
        public @Nullable List<SpellDatum<?>> getHex(ServerLevel level) {
            return this.hexHolder.getHex(this.stack, level);
        }

        @Override
        public void writeHex(List<SpellDatum<?>> patterns, int mana) {
            this.hexHolder.writeHex(this.stack, patterns, mana);
        }

        @Override
        public void clearHex() {
            this.hexHolder.clearHex(this.stack);
        }
    }
}
