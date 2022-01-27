package at.petrak.hexcasting.common.casting.operators.spells.sentinel;

import at.petrak.hexcasting.HexUtils;
import at.petrak.hexcasting.common.lib.LibCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

// it *really* doesn't like this being kotlin
public class CapSentinel implements
    ICapabilitySerializable<CompoundTag> {
    public static final String CAP_NAME = "sentinel";
    public static final String TAG_EXISTS = "exists";
    public static final String TAG_POSITION = "position";
    public static final String TAG_COLOR = "color";

    public boolean hasSentinel;
    public Vec3 position;
    public int color;

    public CapSentinel(boolean hasSentinel, Vec3 position, int color) {
        this.hasSentinel = hasSentinel;
        this.position = position;
        this.color = color;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction dir) {
        return LibCapabilities.SENTINEL.orEmpty(cap, LazyOptional.of(() -> this));
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putBoolean(TAG_EXISTS, this.hasSentinel);
        tag.put(TAG_POSITION, HexUtils.serializeToNBT(this.position));
        tag.putInt(TAG_COLOR, this.color);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.hasSentinel = tag.getBoolean(TAG_EXISTS);
        this.position = HexUtils.DeserializeVec3FromNBT(tag.getLongArray(TAG_POSITION));
        this.color = tag.getInt(TAG_COLOR);
    }
}
