package at.petrak.hexcasting.fabric.cc.adimpl;

import at.petrak.hexcasting.api.addldata.ADPigment;
import at.petrak.hexcasting.api.item.PigmentItem;
import at.petrak.hexcasting.api.pigment.ColorProvider;
import at.petrak.hexcasting.fabric.cc.HexCardinalComponents;
import org.ladysnake.cca.api.v3.component.TransientComponent;
import org.ladysnake.cca.api.v3.item.ItemComponent;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * The pigment itself
 */
public abstract class CCPigment implements ADPigment, TransientComponent {

    public final ItemStack stack;

    public CCPigment(ItemStack stack) {
        this.stack = stack;
    }

    public static class ItemBased extends CCPigment {
        private final PigmentItem item;

        public ItemBased(ItemStack owner) {
            super(owner);
            var item = owner.getItem();
            if (!(item instanceof PigmentItem col)) {
                throw new IllegalStateException("item is not a pigment: " + owner);
            }
            this.item = col;
        }

        @Override
        public ColorProvider provideColor(UUID owner) {
            return item.provideColor(this.stack, owner);
        }
    }
}
