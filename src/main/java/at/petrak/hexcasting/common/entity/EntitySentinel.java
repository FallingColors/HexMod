package at.petrak.hexcasting.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class EntitySentinel extends Entity {
    public UUID owner;

    public EntitySentinel(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public EntitySentinel(Level level, Player owner) {
        this(HexEntities.SENTINEL.get(), level);

    }

    @Override

    protected void defineSynchedData() {
            
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    public Packet<?> getAddEntityPacket() {
        // No additional data required
        return new ClientboundAddEntityPacket(this, 0);
    }
}
