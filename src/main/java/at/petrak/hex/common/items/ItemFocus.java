package at.petrak.hex.common.items;

import at.petrak.hex.HexMod;
import at.petrak.hex.common.casting.CastingContext;
import at.petrak.hex.common.casting.SpellDatum;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class ItemFocus extends ItemDataHolder {
    public static final ResourceLocation PREDICATE = new ResourceLocation(HexMod.MOD_ID, "datatype");
    public static final String TAG_DATA = "data";

    public ItemFocus(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @Nullable SpellDatum<?> readDatum(CompoundTag tag, CastingContext ctx) {
        try {
            return SpellDatum.DeserializeFromNBT(tag.getCompound(TAG_DATA), ctx);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void writeDatum(CompoundTag tag, SpellDatum<?> datum) {
        tag.put(TAG_DATA, datum.serializeToNBT());
    }
}
