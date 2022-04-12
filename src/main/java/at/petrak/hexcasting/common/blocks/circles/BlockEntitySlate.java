package at.petrak.hexcasting.common.blocks.circles;

import at.petrak.hexcasting.common.blocks.HexBlockEntities;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.paucal.api.PaucalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockEntitySlate extends PaucalBlockEntity {
    public static final String TAG_PATTERN = "pattern";

    @Nullable
    public HexPattern pattern;

    public BlockEntitySlate(BlockPos pos, BlockState state) {
        super(HexBlockEntities.SLATE_TILE.get(), pos, state);
    }

    @Override
    protected void saveModData(CompoundTag tag) {
        if (this.pattern != null) {
            tag.put(TAG_PATTERN, this.pattern.serializeToNBT());
        } else {
            tag.put(TAG_PATTERN, new CompoundTag());
        }
    }

    @Override
    protected void loadModData(CompoundTag tag) {
        if (tag.contains(TAG_PATTERN, Tag.TAG_COMPOUND)) {
            CompoundTag patternTag = tag.getCompound(TAG_PATTERN);
            if (HexPattern.IsHexPattern(patternTag))
                this.pattern = HexPattern.DeserializeFromNBT(patternTag);
            else
                this.pattern = null;
        } else
            this.pattern = null;
    }

}
