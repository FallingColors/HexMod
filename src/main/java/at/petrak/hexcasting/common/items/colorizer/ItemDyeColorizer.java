package at.petrak.hexcasting.common.items.colorizer;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

public class ItemDyeColorizer extends Item {
    private final DyeColor dyeColor;

    public ItemDyeColorizer(DyeColor dyeColor, Properties pProperties) {
        super(pProperties);
        this.dyeColor = dyeColor;
    }

    public DyeColor getDyeColor() {
        return dyeColor;
    }
}
