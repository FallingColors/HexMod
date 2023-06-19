package at.petrak.hexcasting.common.items.pigment;

import at.petrak.hexcasting.api.addldata.ADPigment;
import at.petrak.hexcasting.api.item.PigmentItem;
import at.petrak.hexcasting.api.pigment.ColorProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class ItemAmethystAndCopperPigment extends Item implements PigmentItem {
    public ItemAmethystAndCopperPigment(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ColorProvider provideColor(ItemStack stack, UUID owner) {
        return colorProvider;
    }

    protected MyColorProvider colorProvider = new MyColorProvider();

    protected class MyColorProvider extends ColorProvider {
        private static final int[] COLORS = {
            0xff_54398a, // dark purple
            0xff_cfa0f3, // light purple
            0xff_fecbe6, // pink
            0xff_cfa0f3, // light purple
            0xff_e77c56, // dark copper
        };

        @Override
        protected int getRawColor(float time, Vec3 position) {
            return ADPigment.morphBetweenColors(COLORS, new Vec3(0.1, 0.1, 0.1), time / 600, position);
        }
    }
}
