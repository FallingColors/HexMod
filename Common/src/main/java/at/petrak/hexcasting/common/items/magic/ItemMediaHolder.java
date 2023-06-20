package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.api.item.MediaHolderItem;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.utils.MathUtils;
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
    public static final String TAG_MEDIA = "hexcasting:media";
    public static final String TAG_MAX_MEDIA = "hexcasting:start_media";

    public static final TextColor HEX_COLOR = TextColor.fromRgb(0xb38ef3);

    private static final DecimalFormat PERCENTAGE = new DecimalFormat("####");

    static {
        PERCENTAGE.setRoundingMode(RoundingMode.DOWN);
    }

    private static final DecimalFormat DUST_AMOUNT = new DecimalFormat("###,###.##");

    public ItemMediaHolder(Properties pProperties) {
        super(pProperties);
    }

    public static ItemStack withMedia(ItemStack stack, long media, long maxMedia) {
        Item item = stack.getItem();
        if (item instanceof ItemMediaHolder) {
            NBTHelper.putLong(stack, TAG_MEDIA, media);
            NBTHelper.putLong(stack, TAG_MAX_MEDIA, maxMedia);
        }

        return stack;
    }

    @Override
    public long getMedia(ItemStack stack) {
        if (NBTHelper.hasInt(stack, TAG_MEDIA))
            return NBTHelper.getInt(stack, TAG_MEDIA);

        return NBTHelper.getLong(stack, TAG_MEDIA);
    }

    @Override
    public long getMaxMedia(ItemStack stack) {
        if (NBTHelper.hasInt(stack, TAG_MAX_MEDIA))
            return NBTHelper.getInt(stack, TAG_MAX_MEDIA);

        return NBTHelper.getLong(stack, TAG_MAX_MEDIA);
    }

    @Override
    public void setMedia(ItemStack stack, long media) {
        NBTHelper.putLong(stack, TAG_MEDIA, MathUtils.clamp(media, 0, getMaxMedia(stack)));
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return getMaxMedia(pStack) > 0;
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        var media = getMedia(pStack);
        var maxMedia = getMaxMedia(pStack);
        return MediaHelper.mediaBarColor(media, maxMedia);
    }

    @Override
    public int getBarWidth(ItemStack pStack) {
        var media = getMedia(pStack);
        var maxMedia = getMaxMedia(pStack);
        return MediaHelper.mediaBarWidth(media, maxMedia);
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents,
        TooltipFlag pIsAdvanced) {
        var maxMedia = getMaxMedia(pStack);
        if (maxMedia > 0) {
            var media = getMedia(pStack);
            var fullness = getMediaFullness(pStack);

            var color = TextColor.fromRgb(MediaHelper.mediaBarColor(media, maxMedia));

            var mediamount = Component.literal(DUST_AMOUNT.format(media / (float) MediaConstants.DUST_UNIT));
            var percentFull = Component.literal(PERCENTAGE.format(100f * fullness) + "%");
            var maxCapacity = Component.translatable("hexcasting.tooltip.media", DUST_AMOUNT.format(maxMedia / (float) MediaConstants.DUST_UNIT));

            mediamount.withStyle(style -> style.withColor(HEX_COLOR));
            maxCapacity.withStyle(style -> style.withColor(HEX_COLOR));
            percentFull.withStyle(style -> style.withColor(color));

            pTooltipComponents.add(
                Component.translatable("hexcasting.tooltip.media_amount.advanced",
                    mediamount, maxCapacity, percentFull));
        }

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
