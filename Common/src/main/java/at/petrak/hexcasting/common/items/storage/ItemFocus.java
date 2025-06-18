package at.petrak.hexcasting.common.items.storage;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.item.VariantItem;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ItemFocus extends Item implements IotaHolderItem, VariantItem {
    // 0 = no overlay
    // 1 = unsealed
    // 2 = sealed
    public static final ResourceLocation OVERLAY_PRED = modLoc("overlay_layer");
    public static final ResourceLocation VARIANT_PRED = modLoc("variant");
    public static final int NUM_VARIANTS = 8;

    public ItemFocus(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return super.getDescriptionId(stack) + (stack.has(HexDataComponents.SEALED) ? ".sealed" : "");
    }

    @Override
    public boolean writeable(ItemStack stack) {
        return !isSealed(stack);
    }

    @Override
    public boolean canWrite(ItemStack stack, Iota datum) {
        return datum == null || !isSealed(stack);
    }

    @Override
    public void writeDatum(ItemStack stack, Iota datum) {
        if (datum == null) {
            stack.remove(HexDataComponents.IOTA);
            stack.remove(HexDataComponents.SEALED);
        } else if (!isSealed(stack)) {
            stack.set(HexDataComponents.IOTA, datum);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        IotaHolderItem.appendHoverText(this, stack, tooltipComponents, tooltipFlag);
    }

    public static boolean isSealed(ItemStack stack) {
        return stack.has(HexDataComponents.SEALED);
    }

    public static void seal(ItemStack stack) {
        stack.set(HexDataComponents.SEALED, Unit.INSTANCE);
    }

    @Override
    public int numVariants() {
        return NUM_VARIANTS;
    }

    @Override
    public void setVariant(ItemStack stack, int variant) {
        if (!isSealed(stack))
            VariantItem.super.setVariant(stack, variant);
    }
}
