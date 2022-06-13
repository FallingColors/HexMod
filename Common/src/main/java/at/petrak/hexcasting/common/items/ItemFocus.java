package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.spell.LegacySpellDatum;
import at.petrak.hexcasting.api.spell.Widget;
import at.petrak.hexcasting.api.utils.NBTHelper;
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
    public static final ResourceLocation DATATYPE_PRED = modLoc("datatype");
    public static final ResourceLocation SEALED_PRED = modLoc("sealed");

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
    public @Nullable LegacySpellDatum<?> emptyIota(ItemStack stack) {
        return LegacySpellDatum.make(Widget.NULL);
    }

    @Override
    public boolean canWrite(ItemStack stack, LegacySpellDatum<?> datum) {
        return datum == null || !NBTHelper.getBoolean(stack, TAG_SEALED);
    }

    @Override
    public void writeDatum(ItemStack stack, LegacySpellDatum<?> datum) {
        if (datum == null) {
            stack.removeTagKey(TAG_DATA);
            stack.removeTagKey(TAG_SEALED);
        } else if (!NBTHelper.getBoolean(stack, TAG_SEALED)) {
            NBTHelper.put(stack, TAG_DATA, datum.serializeToNBT());
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents,
        TooltipFlag pIsAdvanced) {
        IotaHolderItem.appendHoverText(this, pStack, pTooltipComponents, pIsAdvanced);
    }
}
