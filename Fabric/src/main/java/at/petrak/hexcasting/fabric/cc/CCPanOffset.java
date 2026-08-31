package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.utils.HexUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public class CCPanOffset implements Component, AutoSyncedComponent {
    public static final String TAG_PAN_OFFSET = "pan_offset";

    private final Player owner;
    private Vec2 panOffset = Vec2.ZERO;

    public CCPanOffset(ServerPlayer owner) {
        this.owner = owner;
    }

    public Vec2 getPanOffset() { return panOffset; }
    public void setPanOffset(Vec2 newOffset) { this.panOffset = newOffset; }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.panOffset = HexUtils.vec2FromNBT(tag.getCompound(TAG_PAN_OFFSET));
    }

    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.put(TAG_PAN_OFFSET, HexUtils.serializeToNBT(this.panOffset));
    }
}
