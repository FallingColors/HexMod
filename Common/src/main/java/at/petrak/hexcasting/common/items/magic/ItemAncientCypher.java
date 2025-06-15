package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.lib.HexDataComponents;
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
    public static final String TAG_HEX_NAME = "hex_name";

    public ItemAncientCypher(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void clearHex(ItemStack stack) {
        super.clearHex(stack);
        stack.remove(HexDataComponents.HEX_NAME);
    }

    @Override
    public Component getName(ItemStack stack) {
        var descID = this.getDescriptionId(stack);
        var hexName = stack.get(HexDataComponents.HEX_NAME);
        if (hexName != null) {
            return Component.translatable(descID + ".preset", Component.translatable(hexName));
        } else {
            return Component.translatable(descID);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        // display media fullness as usual
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        var patterns = stack.get(HexDataComponents.PATTERNS);

        // also show contained spell
        if(patterns != null) {
            var storedHex = Component.translatable("hexcasting.tooltip.stored_hex");

            for(var iota : patterns) {
                storedHex.append(iota.display().plainCopy().withStyle(ChatFormatting.DARK_PURPLE));
            }

            tooltipComponents.add(storedHex);
        }
    }
}
