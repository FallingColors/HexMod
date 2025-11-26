package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.api.item.MediaHolderItem;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.utils.MathUtils;
import at.petrak.hexcasting.api.utils.MediaHelper;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.lib.HexDataComponents;
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
            stack.set(HexDataComponents.MEDIA, media);
            stack.set(HexDataComponents.MEDIA_MAX, media);
        }

        return stack;
    }

    @Override
    public long getMedia(ItemStack stack) {
        var media = stack.get(HexDataComponents.MEDIA);
        return media != null ? media : 0L;
    }

    @Override
    public long getMaxMedia(ItemStack stack) {
        var maxMedia = stack.get(HexDataComponents.MEDIA_MAX);
        return maxMedia != null ? maxMedia : 0L;
    }

    @Override
    public void setMedia(ItemStack stack, long media) {
        stack.set(HexDataComponents.MEDIA, MathUtils.clamp(media, 0, getMaxMedia(stack)));
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

    // TODO port: where did it came from?
    /*@Override
    public boolean canBeDepleted() {
        return false;
    }*/

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        var maxMedia = getMaxMedia(stack);
        if (maxMedia > 0) {
            var media = getMedia(stack);
            var fullness = getMediaFullness(stack);

            var color = TextColor.fromRgb(MediaHelper.mediaBarColor(media, maxMedia));

            var mediamount = Component.literal(DUST_AMOUNT.format(media / (float) MediaConstants.DUST_UNIT));
            var percentFull = Component.literal(PERCENTAGE.format(100f * fullness) + "%");
            var maxCapacity = Component.translatable("hexcasting.tooltip.media", DUST_AMOUNT.format(maxMedia / (float) MediaConstants.DUST_UNIT));

            mediamount.withStyle(style -> style.withColor(HEX_COLOR));
            maxCapacity.withStyle(style -> style.withColor(HEX_COLOR));
            percentFull.withStyle(style -> style.withColor(color));

            tooltipComponents.add(
                Component.translatable("hexcasting.tooltip.media_amount.advanced",
                    mediamount, maxCapacity, percentFull));
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
