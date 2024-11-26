package at.petrak.hexcasting.common.items.magic;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ItemMediaBattery extends ItemMediaHolder {
    public static final ResourceLocation MEDIA_PREDICATE = modLoc("media");
    public static final ResourceLocation MAX_MEDIA_PREDICATE = modLoc("max_media");

    public ItemMediaBattery(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean canProvideMedia(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canRecharge(ItemStack stack) {
        return true;
    }
}
