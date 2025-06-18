package at.petrak.hexcasting.common.items.storage;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.lib.HexDataComponents;
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

// Would love to be able to just write to a piece of string but the api requires it to be the same item
public class ItemThoughtKnot extends Item implements IotaHolderItem {
    public static final ResourceLocation WRITTEN_PRED = modLoc("written");

    public ItemThoughtKnot(Properties properties) {
        super(properties);
    }

    @Override
    public boolean writeable(ItemStack stack) {
        return !stack.has(HexDataComponents.IOTA);
    }

    @Override
    public boolean canWrite(ItemStack stack, @Nullable Iota iota) {
        return iota != null && writeable(stack);
    }

    @Override
    public void writeDatum(ItemStack stack, @Nullable Iota iota) {
        if (iota != null) {
            stack.set(HexDataComponents.IOTA, iota);
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext context,
        List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        IotaHolderItem.appendHoverText(this, pStack, pTooltipComponents, pIsAdvanced);
    }
}
