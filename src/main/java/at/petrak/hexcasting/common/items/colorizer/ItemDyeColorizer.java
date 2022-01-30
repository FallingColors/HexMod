package at.petrak.hexcasting.common.items.colorizer;

import net.minecraft.world.item.DyeColor;

public class ItemDyeColorizer extends ItemColorizer {
    private final int dyeIdx;

    public ItemDyeColorizer(int dyeIdx, Properties pProperties) {
        super(pProperties);
        this.dyeIdx = dyeIdx;
    }

    public int getDyeIdx() {
        return dyeIdx;
    }

    @Override
    public int[] getColors() {
        return new int[]{DyeColor.byId(getDyeIdx()).getFireworkColor()};
    }
}
