package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.api.SpellDatum;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemFocus extends ItemDataHolder {
    public static final ResourceLocation DATATYPE_PRED = new ResourceLocation(HexMod.MOD_ID, "datatype");
    public static final String TAG_DATA = "data";
    public static final String TAG_SEALED = "sealed";

    public ItemFocus(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @Nullable CompoundTag readDatumTag(ItemStack stack) {
        if (!stack.hasTag()) {
            return null;
        }
        var tag = stack.getTag();
        if (!tag.contains(TAG_DATA)) {
            return null;
        }

        return tag.getCompound(TAG_DATA);
    }

    @Override
    public void writeDatum(CompoundTag tag, SpellDatum<?> datum) {
        if (!tag.getBoolean(TAG_SEALED)) {
            tag.put(TAG_DATA, datum.serializeToNBT());
        }
    }
}
