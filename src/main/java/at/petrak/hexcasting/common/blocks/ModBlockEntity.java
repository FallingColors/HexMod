package at.petrak.hexcasting.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ModBlockEntity extends BlockEntity {
    public ModBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
    }

    protected abstract void saveModData(CompoundTag tag);

    protected abstract void loadModData(CompoundTag tag);

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        saveModData(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        loadModData(pTag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        var tag = new CompoundTag();
        saveModData(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        loadModData(tag);
    }
}
