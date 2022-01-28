package at.petrak.hexcasting.common.casting.operators.spells.sentinel;

import at.petrak.hexcasting.HexUtils;
import at.petrak.hexcasting.common.lib.HexCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

// it *really* doesn't like this being kotlin
public class CapSentinel implements ICapabilitySerializable<CompoundTag> {
    public static final String CAP_NAME = "sentinel";
    public static final String TAG_EXISTS = "exists";
    public static final String TAG_EXTENDS_RANGE = "extends_range";
    public static final String TAG_POSITION = "position";

    public boolean hasSentinel;
    public boolean extendsRange;
    public Vec3 position;

    public CapSentinel(boolean hasSentinel, boolean extendsRange, Vec3 position) {
        this.hasSentinel = hasSentinel;
        this.extendsRange = extendsRange;
        this.position = position;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction dir) {
        return HexCapabilities.SENTINEL.orEmpty(cap, LazyOptional.of(() -> this));
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putBoolean(TAG_EXISTS, this.hasSentinel);
        tag.putBoolean(TAG_EXTENDS_RANGE, this.extendsRange);
        tag.put(TAG_POSITION, HexUtils.serializeToNBT(this.position));

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.hasSentinel = tag.getBoolean(TAG_EXISTS);
        this.extendsRange = tag.getBoolean(TAG_EXTENDS_RANGE);
        this.position = HexUtils.DeserializeVec3FromNBT(tag.getLongArray(TAG_POSITION));
    }
}
