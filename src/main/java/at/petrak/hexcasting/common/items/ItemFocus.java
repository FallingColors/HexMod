package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.api.item.DataHolder;
import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.common.casting.Widget;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemFocus extends Item implements DataHolder {
    public static final ResourceLocation DATATYPE_PRED = new ResourceLocation(HexMod.MOD_ID, "datatype");
    public static final String TAG_DATA = "data";
    public static final String TAG_SEALED = "sealed";

    public ItemFocus(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @Nullable CompoundTag readDatumTag(ItemStack stack) {
        if (!stack.hasTag()) {
            return SpellDatum.make(Widget.NULL).serializeToNBT();
        }
        var tag = stack.getTag();
        if (!tag.contains(TAG_DATA, Tag.TAG_COMPOUND)) {
            return SpellDatum.make(Widget.NULL).serializeToNBT();
        }

        return tag.getCompound(TAG_DATA);
    }

    @Override
    public boolean canWrite(CompoundTag tag, SpellDatum<?> datum) {
        return !tag.getBoolean(TAG_SEALED);
    }

    @Override
    public void writeDatum(CompoundTag tag, SpellDatum<?> datum) {
        if (!tag.getBoolean(TAG_SEALED)) {
            if (datum == null)
                tag.remove(TAG_DATA);
            else
                tag.put(TAG_DATA, datum.serializeToNBT());
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents,
        TooltipFlag pIsAdvanced) {
        DataHolder.appendHoverText(this, pStack, pTooltipComponents, pIsAdvanced);
    }
}
