package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.api.item.ManaHolderItem;
import at.petrak.hexcasting.api.misc.ManaConstants;
import at.petrak.hexcasting.api.item.MediaHolderItem;
import at.petrak.hexcasting.api.utils.ManaHelper;
import at.petrak.hexcasting.api.utils.NBTHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

public abstract class ItemMediaHolder extends Item implements MediaHolderItem {
    public static final String TAG_MANA = "hexcasting:mana";
    public static final String TAG_MAX_MANA = "hexcasting:start_mana";

    public static final TextColor HEX_COLOR = TextColor.fromRgb(0xb38ef3);

    private static final DecimalFormat PERCENTAGE = new DecimalFormat("####");

    static {
        PERCENTAGE.setRoundingMode(RoundingMode.DOWN);
    }

    private static final DecimalFormat DUST_AMOUNT = new DecimalFormat("###,###.##");

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
        var maxMana = getMaxMana(pStack);
        if (maxMana > 0) {
            var mana = getMana(pStack);
            var fullness = getManaFullness(pStack);

            var color = TextColor.fromRgb(ManaHelper.manaBarColor(mana, maxMana));

            var manaAmount = new TextComponent(DUST_AMOUNT.format(mana / (float) ManaConstants.DUST_UNIT));
            var percentFull = new TextComponent(PERCENTAGE.format(100f * fullness) + "%");
            var maxCapacity = new TranslatableComponent("hexcasting.tooltip.mana", DUST_AMOUNT.format(maxMana / (float) ManaConstants.DUST_UNIT));

            manaAmount.withStyle(style -> style.withColor(HEX_COLOR));
            maxCapacity.withStyle(style -> style.withColor(HEX_COLOR));
            percentFull.withStyle(style -> style.withColor(color));

            pTooltipComponents.add(
                new TranslatableComponent("hexcasting.tooltip.mana_amount.advanced",
                    manaAmount, maxCapacity, percentFull));
        }

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
