package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.api.misc.MediaConstants;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

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

    @Override
    public void fillItemCategory(@NotNull CreativeModeTab tab, @NotNull NonNullList<ItemStack> items) {
        // who was drunk at the wheel when they named this
        if (allowedIn(tab)) {
            var mediamounts = new int[]{
                MediaConstants.CRYSTAL_UNIT,
                20 * MediaConstants.CRYSTAL_UNIT,
                64 * MediaConstants.CRYSTAL_UNIT,
                640 * MediaConstants.CRYSTAL_UNIT,
                6400 * MediaConstants.CRYSTAL_UNIT,
            };
            for (int mediamount : mediamounts) {
                var stack = new ItemStack(this);
                items.add(ItemMediaHolder.withMedia(stack, mediamount, mediamount));
            }
        }
    }
}
