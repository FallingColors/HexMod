package at.petrak.hexcasting.common.items.pigment;

import at.petrak.hexcasting.api.item.PigmentItem;
import at.petrak.hexcasting.api.pigment.ColorProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class ItemAmethystPigment extends Item implements PigmentItem {
    public ItemAmethystPigment(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ColorProvider provideColor(ItemStack stack, UUID owner) {
        return colorProvider;
    }

    protected MyColorProvider colorProvider = new MyColorProvider();

    protected class MyColorProvider extends ColorProvider {
        @Override
        protected int getRawColor(float time, Vec3 position) {
            return 0xff_ab65eb;
        }
    }
}
