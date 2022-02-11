package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.api.SpellDatum;
import at.petrak.hexcasting.common.casting.CastingContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

abstract public class ItemDataHolder extends Item {
    public ItemDataHolder(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    public abstract CompoundTag readDatumTag(ItemStack stack);

    @Nullable
    public SpellDatum<?> readDatum(ItemStack stack, CastingContext ctx) {
        if (!(stack.getItem() instanceof ItemDataHolder dh)) {
            throw new IllegalArgumentException("stack's item must be an ItemDataholder but was " + stack.getItem());
        }

        var tag = dh.readDatumTag(stack);
        if (tag != null) {
            return SpellDatum.DeserializeFromNBT(tag, ctx);
        } else {
            return null;
        }
    }

    public abstract void writeDatum(CompoundTag tag, SpellDatum<?> datum);

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents,
        TooltipFlag pIsAdvanced) {
        var datumTag = this.readDatumTag(pStack);
        if (datumTag != null) {
            var component = SpellDatum.DisplayFromTag(datumTag);
            pTooltipComponents.add(new TranslatableComponent("hexcasting.spelldata.onitem", component));

            if (pIsAdvanced.isAdvanced()) {
                pTooltipComponents.add(NbtUtils.toPrettyComponent(datumTag));
            }
        }
    }
}
