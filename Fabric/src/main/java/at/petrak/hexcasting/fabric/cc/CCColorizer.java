package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.addldata.Colorizer;
import at.petrak.hexcasting.api.item.ColorizerItem;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * The colorizer itself
 */
public abstract class CCColorizer extends ItemComponent implements Colorizer {
    public CCColorizer(ItemStack stack) {
        super(stack, HexCardinalComponents.COLORIZER);
    }

    public static class ItemBased extends CCColorizer {
        private final ColorizerItem item;

        public ItemBased(ItemStack owner) {
            super(owner);
            var item = owner.getItem();
            if (!(item instanceof ColorizerItem col)) {
                throw new IllegalStateException("item is not a colorizer: " + owner);
            }
            this.item = col;
        }

        @Override
        public int color(UUID owner, float time, Vec3 position) {
            return item.color(this.stack, owner, time, position);
        }
    }
}
