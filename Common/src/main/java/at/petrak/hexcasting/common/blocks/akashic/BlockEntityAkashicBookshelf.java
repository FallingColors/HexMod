package at.petrak.hexcasting.common.blocks.akashic;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import at.petrak.hexcasting.api.spell.DatumType;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.common.lib.HexBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockEntityAkashicBookshelf extends HexBlockEntity {
    public static final String TAG_RECORD_POS = "record_pos";
    public static final String TAG_PATTERN = "pattern";

    // This might actually be inaccurate! It's a best-guess
    private BlockPos recordPos = null;
    // This is only not null if this stores any data.
    private HexPattern pattern = null;

    public BlockEntityAkashicBookshelf(BlockPos pWorldPosition, BlockState pBlockState) {
        super(HexBlockEntities.AKASHIC_BOOKSHELF_TILE, pWorldPosition, pBlockState);
    }

    @Nullable
    public BlockPos getRecordPos() {
        return recordPos;
    }

    @Nullable
    public HexPattern getPattern() {
        return pattern;
    }


    public void setNewData(BlockPos recordPos, HexPattern pattern, DatumType type) {
        this.recordPos = recordPos;
        this.pattern = pattern;

        this.setChanged();

        BlockState worldBs = this.level.getBlockState(this.getBlockPos());
        var oldBs = this.getBlockState();

        if (worldBs.getBlock() == oldBs.getBlock()) {
            var newBs = oldBs.setValue(BlockAkashicBookshelf.DATUM_TYPE, type);
            this.level.setBlock(this.getBlockPos(), newBs, 3);
            this.level.sendBlockUpdated(this.getBlockPos(), oldBs, newBs, 3);
        }
    }

    @Override
    protected void saveModData(CompoundTag compoundTag) {
        compoundTag.put(TAG_RECORD_POS,
            this.recordPos == null ? new CompoundTag() : NbtUtils.writeBlockPos(this.recordPos));
        compoundTag.put(TAG_PATTERN, this.pattern == null ? new CompoundTag() : this.pattern.serializeToNBT());
    }

    @Override
    protected void loadModData(CompoundTag compoundTag) {
        CompoundTag recordPos = compoundTag.getCompound(TAG_RECORD_POS);
        CompoundTag pattern = compoundTag.getCompound(TAG_PATTERN);

        if (recordPos.contains("X", Tag.TAG_ANY_NUMERIC) &&
            recordPos.contains("Y", Tag.TAG_ANY_NUMERIC) &&
            recordPos.contains("Z", Tag.TAG_ANY_NUMERIC)) {
            this.recordPos = NbtUtils.readBlockPos(recordPos);
        } else {
            this.recordPos = null;
        }
        if (HexPattern.isPattern(pattern)) {
            this.pattern = HexPattern.fromNBT(pattern);
        } else {
            this.pattern = null;
        }
    }

}
