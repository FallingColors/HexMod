package at.petrak.hexcasting.fabric.cc;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class CCBrainswept implements Component, AutoSyncedComponent {
    public static final String TAG_BRAINSWEPT = "brainswept";

    private final LivingEntity owner;

    public CCBrainswept(LivingEntity owner) {
        this.owner = owner;
    }

    private boolean brainswept = false;

    public boolean isBrainswept() {
        return this.brainswept;
    }

    public void setBrainswept(boolean brainswept) {
        this.brainswept = brainswept;
        HexCardinalComponents.BRAINSWEPT.sync(this.owner);
    }

    @Override
    public void applySyncPacket(FriendlyByteBuf buf) {
        AutoSyncedComponent.super.applySyncPacket(buf);
        if (owner instanceof Mob mob && brainswept)
            mob.removeFreeWill();
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        this.brainswept = tag.getBoolean(TAG_BRAINSWEPT);
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putBoolean(TAG_BRAINSWEPT, this.brainswept);
    }
}
