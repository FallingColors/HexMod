package at.petrak.hexcasting.common.blocks.akashic;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.client.render.HexPatternPoints;
import at.petrak.hexcasting.common.lib.HexBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockEntityAkashicBookshelf extends HexBlockEntity {
    public static final String TAG_PATTERN = "pattern";
    public static final String TAG_IOTA = "iota";
    public static final String TAG_DUMMY = "dummy";

    // This is only not null if this stores any data.
    private HexPattern pattern = null;
    // TODO port: check if it works
    // When the world is first loading we can sometimes try to deser this from nbt without the world existing yet.
    // We also need a way to display the iota to the client.
    // For both these cases we save just the tag of the iota.
    private Iota iota = null;

    public HexPatternPoints points;

    public BlockEntityAkashicBookshelf(BlockPos pWorldPosition, BlockState pBlockState) {
        super(HexBlockEntities.AKASHIC_BOOKSHELF_TILE, pWorldPosition, pBlockState);
    }

    @Nullable
    public HexPattern getPattern() {
        return pattern;
    }

    @Nullable
    public Iota getIota() {
        return iota;
    }

    /*@Nullable
    public Tag getIotaTag() {
        return iotaTag;
    }*/

    public void setNewMapping(HexPattern pattern, Iota iota) {
        var previouslyEmpty = this.pattern == null;
        this.pattern = pattern;
        this.iota = iota;
        //this.iotaTag = IotaType.TYPED_CODEC.encodeStart(NbtOps.INSTANCE, iota).getOrThrow();

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
        //this.iotaTag = null;
        this.iota = null;

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
    protected void saveModData(CompoundTag compoundTag, HolderLookup.Provider registries) {
        if (this.pattern != null && this.iota != null) {
            compoundTag.put(TAG_PATTERN, HexPattern.CODEC.encodeStart(NbtOps.INSTANCE, pattern).getOrThrow());
            compoundTag.put(TAG_IOTA, IotaType.TYPED_CODEC.encodeStart(NbtOps.INSTANCE, iota).getOrThrow());
        } else {
            compoundTag.putBoolean(TAG_DUMMY, false);
        }
    }

    @Override
    protected void loadModData(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains(TAG_PATTERN) && tag.contains(TAG_IOTA)) {
            this.pattern = HexPattern.CODEC.parse(NbtOps.INSTANCE, tag.getCompound(TAG_PATTERN)).getOrThrow();
            //this.iotaTag = tag.getCompound(TAG_IOTA);
            this.iota = IotaType.TYPED_CODEC.parse(NbtOps.INSTANCE, tag.getCompound(TAG_IOTA)).getOrThrow();
        } else if (tag.contains(TAG_DUMMY)) {
            this.pattern = null;
            //this.iotaTag = null;
            this.iota = null;
        }
    }
}
