package at.petrak.hexcasting.api.item;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.client.ClientTickCounter;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
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
     * {@link at.petrak.hexcasting.api.casting.iota.IotaType IotaType} given by the resource location.
     * <p>
     * This is not useful to the player at all.
     */
    String TAG_OVERRIDE_VISUALLY = "VisualOverride";

    @Nullable
    CompoundTag readIotaTag(ItemStack stack);

    @Nullable
    default Iota readIota(ItemStack stack, ServerLevel world) {
        if (!(stack.getItem() instanceof IotaHolderItem dh)) {
            // this should be checked via mishap beforehand
            throw new IllegalArgumentException("stack's item must be an IotaHolderItem but was " + stack.getItem());
        }

        var tag = dh.readIotaTag(stack);
        if (tag != null) {
            return IotaType.deserialize(tag, world);
        } else {
            return null;
        }
    }

    /**
     * What is this considered to contain when nothing can be read?
     */
    @Nullable
    default Iota emptyIota(ItemStack stack) {
        return null;
    }

    default int getColor(ItemStack stack) {
        if (NBTHelper.hasString(stack, TAG_OVERRIDE_VISUALLY)) {
            var override = NBTHelper.getString(stack, TAG_OVERRIDE_VISUALLY);

            if (override != null && ResourceLocation.isValidResourceLocation(override)) {
                var key = new ResourceLocation(override);
                if (HexIotaTypes.REGISTRY.containsKey(key)) {
                    var iotaType = HexIotaTypes.REGISTRY.get(key);
                    if (iotaType != null) {
                        return iotaType.color();
                    }
                }
            }

            return 0xFF000000 | Mth.hsvToRgb(ClientTickCounter.getTotal() * 2 % 360 / 360F, 0.75F, 1F);
        }

        var tag = this.readIotaTag(stack);
        if (tag == null) {
            return HexUtils.ERROR_COLOR;
        }

        return IotaType.getColor(tag);
    }

    /**
     * @return whether it is possible to write to this IotaHolder
     */
    boolean writeable(ItemStack stack);

    /**
     * Write {@code null} to indicate erasing
     */
    boolean canWrite(ItemStack stack, @Nullable Iota iota);

    /**
     * Write {@code null} to indicate erasing
     */
    void writeDatum(ItemStack stack, @Nullable Iota iota);

    static void appendHoverText(IotaHolderItem self, ItemStack stack, List<Component> components,
        TooltipFlag flag) {
        var datumTag = self.readIotaTag(stack);
        if (datumTag != null) {
            var cmp = IotaType.getDisplay(datumTag);
            components.add(Component.translatable("hexcasting.spelldata.onitem", cmp));

            if (flag.isAdvanced()) {
                components.add(Component.literal("").append(NbtUtils.toPrettyComponent(datumTag)));
            }
        } else if (NBTHelper.hasString(stack, IotaHolderItem.TAG_OVERRIDE_VISUALLY)) {
            components.add(Component.translatable("hexcasting.spelldata.onitem",
                Component.translatable("hexcasting.spelldata.anything").withStyle(ChatFormatting.LIGHT_PURPLE)));
        }
    }
}
