package at.petrak.hexcasting.api.item;

import at.petrak.hexcasting.api.spell.LegacySpellDatum;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface DataHolderItem {
    String TAG_OVERRIDE_VISUALLY = "VisualOverride";

    @Nullable CompoundTag readDatumTag(ItemStack stack);

    @Nullable
    default LegacySpellDatum<?> readDatum(ItemStack stack, ServerLevel world) {
        if (!(stack.getItem() instanceof DataHolderItem dh)) {
            // this should be checked via mishap beforehand
            throw new IllegalArgumentException("stack's item must be an ItemDataholder but was " + stack.getItem());
        }

        var tag = dh.readDatumTag(stack);
        if (tag != null) {
            return LegacySpellDatum.fromNBT(tag, world);
        } else {
            return null;
        }
    }

    @Nullable
    default LegacySpellDatum<?> emptyDatum(ItemStack stack) {
        return null;
    }

    boolean canWrite(ItemStack stack, @Nullable LegacySpellDatum<?> datum);

    void writeDatum(ItemStack stack, @Nullable LegacySpellDatum<?> datum);

    static void appendHoverText(DataHolderItem self, ItemStack pStack, List<Component> pTooltipComponents,
        TooltipFlag pIsAdvanced) {
        var datumTag = self.readDatumTag(pStack);
        if (datumTag != null) {
            var component = LegacySpellDatum.displayFromNBT(datumTag);
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
