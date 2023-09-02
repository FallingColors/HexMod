package at.petrak.hexcasting.common.items.colorizer;

import at.petrak.hexcasting.api.item.ColorizerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class ItemDyeColorizer extends Item implements ColorizerItem {
    private final DyeColor dyeColor;

    public ItemDyeColorizer(DyeColor dyeColor, Properties pProperties) {
        super(pProperties);
        this.dyeColor = dyeColor;
    }

    public DyeColor getDyeColor() {
        return dyeColor;
    }

    @Override
    public int color(ItemStack stack, UUID owner, float time, Vec3 position) {
        return dyeColor.getTextColor();
    }
}
