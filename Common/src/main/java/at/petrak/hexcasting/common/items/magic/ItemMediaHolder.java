package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.api.item.MediaHolderItem;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.utils.MediaHelper;
import at.petrak.hexcasting.api.utils.NBTHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
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

    public static ItemStack withMedia(ItemStack stack, int mana, int maxMana) {
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
        return MediaHelper.mediaBarColor(mana, maxMana);
    }

    @Override
    public int getBarWidth(ItemStack pStack) {
        var mana = getMedia(pStack);
        var maxMana = getMaxMedia(pStack);
        return MediaHelper.mediaBarWidth(mana, maxMana);
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents,
        TooltipFlag pIsAdvanced) {
        var maxMana = getMaxMedia(pStack);
        if (maxMana > 0) {
            var mana = getMedia(pStack);
            var fullness = getMediaFullness(pStack);

            var color = TextColor.fromRgb(MediaHelper.mediaBarColor(mana, maxMana));

            var manaAmount = Component.literal(DUST_AMOUNT.format(mana / (float) MediaConstants.DUST_UNIT));
            var percentFull = Component.literal(PERCENTAGE.format(100f * fullness) + "%");
            var maxCapacity = Component.translatable("hexcasting.tooltip.mana", DUST_AMOUNT.format(maxMana / (float) MediaConstants.DUST_UNIT));

            manaAmount.withStyle(style -> style.withColor(HEX_COLOR));
            maxCapacity.withStyle(style -> style.withColor(HEX_COLOR));
            percentFull.withStyle(style -> style.withColor(color));

            pTooltipComponents.add(
                Component.translatable("hexcasting.tooltip.mana_amount.advanced",
                    manaAmount, maxCapacity, percentFull));
        }

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
