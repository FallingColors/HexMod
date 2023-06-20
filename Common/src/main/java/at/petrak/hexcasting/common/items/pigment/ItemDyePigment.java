package at.petrak.hexcasting.common.items.pigment;

import at.petrak.hexcasting.api.item.PigmentItem;
import at.petrak.hexcasting.api.pigment.ColorProvider;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class ItemDyePigment extends Item implements PigmentItem {
    private final DyeColor dyeColor;

    public ItemDyePigment(DyeColor dyeColor, Properties pProperties) {
        super(pProperties);
        this.dyeColor = dyeColor;
    }

    public DyeColor getDyeColor() {
        return dyeColor;
    }

    @Override
    public ColorProvider provideColor(ItemStack stack, UUID owner) {
        return colorProvider;
    }

    protected MyColorProvider colorProvider = new MyColorProvider();

    protected class MyColorProvider extends ColorProvider {
        @Override
        protected int getRawColor(float time, Vec3 position) {
            return dyeColor.getTextColor();
        }
    }
}
