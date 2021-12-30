package at.petrak.hex.common.items;

import at.petrak.hex.common.casting.CastingContext;
import at.petrak.hex.common.casting.SpellDatum;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

abstract public class ItemDataHolder extends Item {
    public ItemDataHolder(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    public abstract SpellDatum<?> readDatum(CompoundTag tag, CastingContext ctx);

    public abstract void writeDatum(CompoundTag tag, SpellDatum<?> datum);
}
