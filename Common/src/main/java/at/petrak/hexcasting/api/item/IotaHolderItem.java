package at.petrak.hexcasting.api.item;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.client.ClientTickCounter;
import at.petrak.hexcasting.common.lib.HexDataComponents;
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

    @Nullable
    default Iota readIota(ItemStack stack) {
        if (!(stack.getItem() instanceof IotaHolderItem)) {
            // this should be checked via mishap beforehand
            throw new IllegalArgumentException("stack's item must be an IotaHolderItem but was " + stack.getItem());
        }

        return stack.get(HexDataComponents.IOTA);
    }

    /**
     * What is this considered to contain when nothing can be read?
     */
    @Nullable
    default Iota emptyIota(ItemStack stack) {
        return null;
    }

    default int getColor(ItemStack stack) {
        var override = stack.get(HexDataComponents.VISUAL_OVERRIDE);
        //noinspection OptionalAssignedToNull
        if (override != null) {
            return override.map(IotaType::color).orElseGet(() -> 0xFF000000 | Mth.hsvToRgb(ClientTickCounter.getTotal() * 2 % 360 / 360F, 0.75F, 1F));
        }

        var iota = this.readIota(stack);
        if (iota == null) {
            return HexUtils.ERROR_COLOR;
        }

        return iota.getType().color();
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
        var datum = self.readIota(stack);
        if (datum != null) {
            var cmp = datum.display();
            components.add(Component.translatable("hexcasting.spelldata.onitem", cmp));

            //TODO port: show NBT in advanced display
            /*if (flag.isAdvanced()) {
                components.add(Component.literal("").append(NbtUtils.toPrettyComponent(datum)));
            }*/
        } else if (stack.has(HexDataComponents.VISUAL_OVERRIDE)) {
            components.add(Component.translatable("hexcasting.spelldata.onitem",
                Component.translatable("hexcasting.spelldata.anything").withStyle(ChatFormatting.LIGHT_PURPLE)));
        }
    }
}
