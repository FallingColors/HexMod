package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.common.casting.ManaHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ItemManaHolder extends Item {
    public static final String TAG_MANA = "hexcasting:mana";
    public static final String TAG_MAX_MANA = "hexcasting:start_mana";

    public ItemManaHolder(Properties pProperties) {
        super(pProperties);
    }

    public int getManaAmt(CompoundTag tag) {
        return tag.getInt(TAG_MANA);
    }

    public int getMaxManaAmt(CompoundTag tag) {
        return tag.getInt(TAG_MAX_MANA);
    }

    public float getManaFullness(CompoundTag tag) {
        return (float) getManaAmt(tag) / (float) getMaxManaAmt(tag);
    }

    /**
     * Return the actual amount of mana extracted.
     */
    public int withdrawMana(CompoundTag tag, int cost) {
        var manaHere = getManaAmt(tag);
        var manaLeft = manaHere - cost;
        tag.putInt(TAG_MANA, Math.max(0, manaLeft));
        return Math.min(cost, manaHere);
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        var tag = pStack.getOrCreateTag();
        return tag.contains(TAG_MANA);
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        var tag = pStack.getOrCreateTag();
        var mana = getManaAmt(tag);
        var maxMana = getMaxManaAmt(tag);
        return ManaHelper.INSTANCE.barColor(mana, maxMana);
    }

    @Override
    public int getBarWidth(ItemStack pStack) {
        var tag = pStack.getOrCreateTag();
        var mana = tag.getInt(TAG_MANA);
        var maxMana = tag.getInt(TAG_MAX_MANA);
        return ManaHelper.INSTANCE.barWidth(mana, maxMana);
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
        if (pIsAdvanced.isAdvanced()) {
            var imh = (ItemManaHolder) pStack.getItem();
            var tag = pStack.getOrCreateTag();
            if (tag.contains(TAG_MANA) && tag.contains(TAG_MAX_MANA)) {
                pTooltipComponents.add(
                    new TranslatableComponent("item.hexcasting.manaholder.amount",
                        String.format("%,d", imh.getManaAmt(tag)),
                        String.format("%,d", imh.getMaxManaAmt(tag)),
                        100f * imh.getManaFullness(tag)).withStyle(ChatFormatting.GRAY));
            }
        }

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
