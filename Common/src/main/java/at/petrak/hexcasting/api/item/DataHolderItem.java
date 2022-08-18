package at.petrak.hexcasting.api.item;

import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.api.utils.NBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@ApiStatus.OverrideOnly
public interface DataHolderItem {
    String TAG_OVERRIDE_VISUALLY = "VisualOverride";

    @Nullable CompoundTag readDatumTag(ItemStack stack);

    @Nullable
    default SpellDatum<?> readDatum(ItemStack stack, ServerLevel world) {
        var tag = readDatumTag(stack);
        if (tag != null) {
            return SpellDatum.fromNBT(tag, world);
        } else {
            return null;
        }
    }

    @Nullable
    default SpellDatum<?> emptyDatum(ItemStack stack) {
        return null;
    }

    boolean canWrite(ItemStack stack, @Nullable SpellDatum<?> datum);

    void writeDatum(ItemStack stack, @Nullable SpellDatum<?> datum);

    static void appendHoverText(DataHolderItem self, ItemStack pStack, List<Component> pTooltipComponents,
                                TooltipFlag pIsAdvanced) {
        var datumTag = self.readDatumTag(pStack);
        if (datumTag != null) {
            var component = SpellDatum.displayFromNBT(datumTag);
            pTooltipComponents.add(new TranslatableComponent("hexcasting.spelldata.onitem", component));

            if (pIsAdvanced.isAdvanced()) {
                pTooltipComponents.add(new TextComponent("").append(NbtUtils.toPrettyComponent(datumTag)));
            }
        } else if (NBTHelper.hasString(pStack, DataHolderItem.TAG_OVERRIDE_VISUALLY)) {
            pTooltipComponents.add(new TranslatableComponent("hexcasting.spelldata.onitem",
                new TranslatableComponent("hexcasting.spelldata.anything").withStyle(ChatFormatting.LIGHT_PURPLE)));

        }
    }
}
