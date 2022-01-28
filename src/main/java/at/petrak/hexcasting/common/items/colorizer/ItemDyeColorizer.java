package at.petrak.hexcasting.common.items.colorizer;

import net.minecraft.world.item.Item;

public class ItemDyeColorizer extends Item {
    private int dyeIdx;

    public ItemDyeColorizer(int dyeIdx, Properties pProperties) {
        super(pProperties);
        this.dyeIdx = dyeIdx;
    }

    public int getDyeIdx() {
        return dyeIdx;
    }
}
