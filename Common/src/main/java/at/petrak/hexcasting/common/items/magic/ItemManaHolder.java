package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.api.item.ManaHolderItem;
import at.petrak.hexcasting.api.utils.ManaHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ItemManaHolder extends Item implements ManaHolderItem {
    private static final String TAG_MANA = "hexcasting:mana";
    private static final String TAG_MAX_MANA = "hexcasting:start_mana";

    public ItemManaHolder(Properties pProperties) {
        super(pProperties);
    }

    public static ItemStack withMana(ItemStack stack, int mana, int maxMana) {
        Item item = stack.getItem();
        if (item instanceof ItemManaHolder) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putInt(TAG_MANA, mana);
            tag.putInt(TAG_MAX_MANA, maxMana);
        }

        return stack;
    }

    @Override
    public int getMana(ItemStack stack) {
        if (!stack.hasTag())
            return 0;
        return stack.getTag().getInt(TAG_MANA);
    }

    @Override
    public int getMaxMana(ItemStack stack) {
        if (!stack.hasTag())
            return 0;
        return stack.getTag().getInt(TAG_MAX_MANA);
    }

    @Override
    public void setMana(ItemStack stack, int mana) {
        stack.getOrCreateTag().putInt(TAG_MANA, Mth.clamp(mana, 0, getMaxMana(stack)));
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return getMaxMana(pStack) > 0;
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        var mana = getMana(pStack);
        var maxMana = getMaxMana(pStack);
        return ManaHelper.barColor(mana, maxMana);
    }

    @Override
    public int getBarWidth(ItemStack pStack) {
        var mana = getMana(pStack);
        var maxMana = getMaxMana(pStack);
        return ManaHelper.barWidth(mana, maxMana);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents,
        TooltipFlag pIsAdvanced) {
        if (pIsAdvanced.isAdvanced() && getMaxMana(pStack) > 0) {
            pTooltipComponents.add(
                new TranslatableComponent("item.hexcasting.manaholder.amount",
                    String.format("%,d", getMana(pStack)),
                    String.format("%,d", getMaxMana(pStack)),
                    100f * getManaFullness(pStack)).withStyle(ChatFormatting.GRAY));
        }

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
