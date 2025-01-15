package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.utils.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemAncientCypher extends ItemCypher {
    public static final String TAG_PATTERNS = "patterns";
    public static final String TAG_HEX_NAME = "hex_name";

    public ItemAncientCypher(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void clearHex(ItemStack stack) {
        super.clearHex(stack);
        NBTHelper.remove(stack, TAG_HEX_NAME);
    }

    @Override
    public Component getName(ItemStack pStack) {
        var descID = this.getDescriptionId(pStack);
        var hexName = NBTHelper.getString(pStack, TAG_HEX_NAME);
        if (hexName != null) {
            return Component.translatable(descID + ".preset", Component.translatable(hexName));
        } else {
            return Component.translatable(descID);
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents,
        TooltipFlag pIsAdvanced) {
        // display media fullness as usual
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);

        // also show contained spell
        var patternsTag = NBTHelper.getList(pStack, TAG_PATTERNS, Tag.TAG_COMPOUND);
        if (patternsTag != null) {
            var storedHex = Component.translatable("hexcasting.tooltip.stored_hex");
            for (var iotaTag : patternsTag) {
                var iotaTagC = NBTHelper.getAsCompound(iotaTag);
                var iotaComponent = IotaType.getDisplay(iotaTagC).copy();
                storedHex.append(iotaComponent.withStyle(ChatFormatting.DARK_PURPLE));
            }
            pTooltipComponents.add(storedHex);
        }
    }
}
