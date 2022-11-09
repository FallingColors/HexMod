package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.spell.iota.Iota;
import at.petrak.hexcasting.api.spell.iota.NullIota;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.lib.HexIotaTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ItemFocus extends Item implements IotaHolderItem {
    // 0 = no overlay
    // 1 = unsealed
    // 2 = sealed
    public static final ResourceLocation OVERLAY_PRED = modLoc("overlay_layer");

    public static final String TAG_DATA = "data";
    public static final String TAG_SEALED = "sealed";

    public ItemFocus(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @Nullable CompoundTag readIotaTag(ItemStack stack) {
        return NBTHelper.getCompound(stack, TAG_DATA);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return super.getDescriptionId(stack) + (NBTHelper.getBoolean(stack, TAG_SEALED) ? ".sealed" : "");
    }

    @Override
    public @Nullable Iota emptyIota(ItemStack stack) {
        return new NullIota();
    }

    @Override
    public boolean canWrite(ItemStack stack, Iota datum) {
        return datum == null || !NBTHelper.getBoolean(stack, TAG_SEALED);
    }

    @Override
    public void writeDatum(ItemStack stack, Iota datum) {
        if (datum == null) {
            stack.removeTagKey(TAG_DATA);
            stack.removeTagKey(TAG_SEALED);
        } else if (!isSealed(stack)) {
            NBTHelper.put(stack, TAG_DATA, HexIotaTypes.serialize(datum));
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents,
        TooltipFlag pIsAdvanced) {
        IotaHolderItem.appendHoverText(this, pStack, pTooltipComponents, pIsAdvanced);
    }

    public static boolean isSealed(ItemStack stack) {
        return NBTHelper.getBoolean(stack, TAG_SEALED);
    }
}
