package at.petrak.hexcasting.api.item;

import at.petrak.hexcasting.api.spell.iota.Iota;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.lib.HexIotaTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Items that store an iota to their tag can implement this interface.
 * <p>
 * On both the Forge and Fabric sides, the registry will be scanned for all items which implement this interface,
 * and the appropriate cap/CC will be attached.
 */
public interface IotaHolderItem {
    /**
     * If this key is set on the item, we ignore the rest of the item and render this as if it were of the
     * {@link at.petrak.hexcasting.api.spell.iota.IotaType IotaType} given by the resource location.
     * <p>
     * This is not useful to the player at all.
     */
    String TAG_OVERRIDE_VISUALLY = "VisualOverride";

    @Nullable CompoundTag readIotaTag(ItemStack stack);

    @Nullable
    default Iota readIota(ItemStack stack, ServerLevel world) {
        if (!(stack.getItem() instanceof IotaHolderItem dh)) {
            // this should be checked via mishap beforehand
            throw new IllegalArgumentException("stack's item must be an IotaHolderItem but was " + stack.getItem());
        }

        var tag = dh.readIotaTag(stack);
        if (tag != null) {
            return HexIotaTypes.deserialize(tag, world);
        } else {
            return null;
        }
    }

    @Nullable
    default Iota emptyIota(ItemStack stack) {
        return null;
    }

    default int getColor(ItemStack stack) {
        var tag = this.readIotaTag(stack);
        if (tag == null) {
            return HexUtils.ERROR_COLOR;
        }

        return HexIotaTypes.getColor(tag);
    }

    boolean canWrite(ItemStack stack, @Nullable Iota iota);

    void writeDatum(ItemStack stack, @Nullable Iota iota);

    static void appendHoverText(IotaHolderItem self, ItemStack stack, List<Component> components,
        TooltipFlag flag) {
        var datumTag = self.readIotaTag(stack);
        if (datumTag != null) {
            var cmp = HexIotaTypes.getDisplay(datumTag);
            components.add(new TranslatableComponent("hexcasting.spelldata.onitem", cmp));

            if (flag.isAdvanced()) {
                components.add(new TextComponent("").append(NbtUtils.toPrettyComponent(datumTag)));
            }
        } else if (NBTHelper.hasString(stack, IotaHolderItem.TAG_OVERRIDE_VISUALLY)) {
            components.add(new TranslatableComponent("hexcasting.spelldata.onitem",
                new TranslatableComponent("hexcasting.spelldata.anything").withStyle(ChatFormatting.LIGHT_PURPLE)));
        }
    }
}
