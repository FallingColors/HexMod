package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.api.item.MediaHolderItem;
import at.petrak.hexcasting.api.utils.ManaHelper;
import at.petrak.hexcasting.api.utils.NBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ItemMediaHolder extends Item implements MediaHolderItem {
    public static final String TAG_MANA = "hexcasting:mana";
    public static final String TAG_MAX_MANA = "hexcasting:start_mana";

    public ItemMediaHolder(Properties pProperties) {
        super(pProperties);
    }

    public static ItemStack withMana(ItemStack stack, int mana, int maxMana) {
        Item item = stack.getItem();
        if (item instanceof ItemMediaHolder) {
            NBTHelper.putInt(stack, TAG_MANA, mana);
            NBTHelper.putInt(stack, TAG_MAX_MANA, maxMana);
        }

        return stack;
    }

    @Override
    public int getMedia(ItemStack stack) {
        return NBTHelper.getInt(stack, TAG_MANA);
    }

    @Override
    public int getMaxMedia(ItemStack stack) {
        return NBTHelper.getInt(stack, TAG_MAX_MANA);
    }

    @Override
    public void setMedia(ItemStack stack, int media) {
        NBTHelper.putInt(stack, TAG_MANA, Mth.clamp(media, 0, getMaxMedia(stack)));
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return getMaxMedia(pStack) > 0;
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        var mana = getMedia(pStack);
        var maxMana = getMaxMedia(pStack);
        return ManaHelper.manaBarColor(mana, maxMana);
    }

    @Override
    public int getBarWidth(ItemStack pStack) {
        var mana = getMedia(pStack);
        var maxMana = getMaxMedia(pStack);
        return ManaHelper.manaBarWidth(mana, maxMana);
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents,
        TooltipFlag pIsAdvanced) {
        if (pIsAdvanced.isAdvanced() && getMaxMedia(pStack) > 0) {
            pTooltipComponents.add(
                new TranslatableComponent("item.hexcasting.manaholder.amount",
                    String.format("%,d", getMedia(pStack)),
                    String.format("%,d", getMaxMedia(pStack)),
                    100f * getManaFullness(pStack)).withStyle(ChatFormatting.GRAY));
        }

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
