package at.petrak.hexcasting.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class HexBlockEntity extends BlockEntity {
    public HexBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
    }

    protected abstract void saveModData(CompoundTag tag, HolderLookup.Provider registries);

    protected abstract void loadModData(CompoundTag tag, HolderLookup.Provider registries);

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider registries) {
        super.saveAdditional(pTag, registries);
        this.saveModData(pTag, registries);
    }

    @Override
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider registries) {
        super.loadAdditional(pTag, registries);
        this.loadModData(pTag, registries);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        this.saveModData(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void sync() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

}
