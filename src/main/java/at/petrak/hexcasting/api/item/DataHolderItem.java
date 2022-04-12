package at.petrak.hexcasting.api.item;

import at.petrak.hexcasting.api.spell.SpellDatum;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface DataHolderItem {
    @Nullable CompoundTag readDatumTag(ItemStack stack);

    @Nullable
    default SpellDatum<?> readDatum(ItemStack stack, ServerLevel world) {
        if (!(stack.getItem() instanceof DataHolderItem dh)) {
            // this should be checked via mishap beforehand
            throw new IllegalArgumentException("stack's item must be an ItemDataholder but was " + stack.getItem());
        }

        var tag = dh.readDatumTag(stack);
        if (tag != null) {
            return SpellDatum.DeserializeFromNBT(tag, world);
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
            var component = SpellDatum.DisplayFromTag(datumTag);
            pTooltipComponents.add(new TranslatableComponent("hexcasting.spelldata.onitem", component));

            if (pIsAdvanced.isAdvanced()) {
                pTooltipComponents.add(NbtUtils.toPrettyComponent(datumTag));
            }
        }
    }
}
