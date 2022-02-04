package at.petrak.hexcasting.common.casting.colors;

import at.petrak.hexcasting.common.lib.HexCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The colorizer item favored by this player.
 */
public class CapPreferredColorizer implements ICapabilitySerializable<CompoundTag> {
    public static final String CAP_NAME = "preferred_colorizer";
    public static final String TAG_COLOR = "colorizer";

    public FrozenColorizer colorizer;

    public CapPreferredColorizer(FrozenColorizer colorizer) {
        this.colorizer = colorizer;
    }


    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return HexCapabilities.PREFERRED_COLORIZER.orEmpty(cap, LazyOptional.of(() -> this));
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.put(TAG_COLOR, this.colorizer.serialize());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains(TAG_COLOR, Tag.TAG_COMPOUND)) {
            var colorizerTag = nbt.getCompound(TAG_COLOR);
            this.colorizer = FrozenColorizer.deserialize(colorizerTag);
        } else {
            this.colorizer = FrozenColorizer.DEFAULT;
        }
    }
}
