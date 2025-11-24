package at.petrak.hexcasting.fabric.cc;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
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
    public void applySyncPacket(RegistryFriendlyByteBuf buf) {
        AutoSyncedComponent.super.applySyncPacket(buf);
        if (owner instanceof Mob mob && brainswept)
            mob.removeFreeWill();
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.brainswept = tag.getBoolean(TAG_BRAINSWEPT);
    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putBoolean(TAG_BRAINSWEPT, this.brainswept);
    }
}
