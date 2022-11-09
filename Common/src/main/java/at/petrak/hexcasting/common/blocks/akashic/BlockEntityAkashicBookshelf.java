package at.petrak.hexcasting.common.blocks.akashic;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import at.petrak.hexcasting.api.spell.iota.Iota;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.common.lib.HexBlockEntities;
import at.petrak.hexcasting.common.lib.HexIotaTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockEntityAkashicBookshelf extends HexBlockEntity {
    public static final String TAG_PATTERN = "pattern";
    public static final String TAG_IOTA = "iota";

    // This is only not null if this stores any data.
    private HexPattern pattern = null;
    // When the world is first loading we can sometimes try to deser this from nbt without the world existing yet.
    // We also need a way to display the iota to the client.
    // For both these cases we save just the tag of the iota.
    private CompoundTag iotaTag = null;

    public BlockEntityAkashicBookshelf(BlockPos pWorldPosition, BlockState pBlockState) {
        super(HexBlockEntities.AKASHIC_BOOKSHELF_TILE, pWorldPosition, pBlockState);
    }

    @Nullable
    public HexPattern getPattern() {
        return pattern;
    }

    @Nullable
    public CompoundTag getIotaTag() {
        return iotaTag;
    }

    public void setNewMapping(HexPattern pattern, Iota iota) {
        var previouslyEmpty = this.pattern == null;
        this.pattern = pattern;
        this.iotaTag = HexIotaTypes.serialize(iota);

        if (previouslyEmpty) {
            var oldBs = this.getBlockState();
            var newBs = oldBs.setValue(BlockAkashicBookshelf.HAS_BOOKS, true);
            this.level.setBlock(this.getBlockPos(), newBs, 3);
            this.level.sendBlockUpdated(this.getBlockPos(), oldBs, newBs, 3);
        } else {
            this.setChanged();
        }
    }

    public void clearIota() {
        var previouslyEmpty = this.pattern == null;
        this.pattern = null;
        this.iotaTag = null;

        this.setChanged();

        if (!previouslyEmpty) {
            var oldBs = this.getBlockState();
            var newBs = oldBs.setValue(BlockAkashicBookshelf.HAS_BOOKS, false);
            this.level.setBlock(this.getBlockPos(), newBs, 3);
            this.level.sendBlockUpdated(this.getBlockPos(), oldBs, newBs, 3);
        } else {
            this.setChanged();
        }
    }

    @Override
    protected void saveModData(CompoundTag compoundTag) {
        if (this.pattern != null && this.iotaTag != null) {
            compoundTag.put(TAG_PATTERN, this.pattern.serializeToNBT());
            compoundTag.put(TAG_IOTA, this.iotaTag);
        }
    }

    @Override
    protected void loadModData(CompoundTag tag) {
        if (tag.contains(TAG_PATTERN) && tag.contains(TAG_IOTA)) {
            this.pattern = HexPattern.fromNBT(tag.getCompound(TAG_PATTERN));
            this.iotaTag = tag.getCompound(TAG_IOTA);
        }
    }
}
