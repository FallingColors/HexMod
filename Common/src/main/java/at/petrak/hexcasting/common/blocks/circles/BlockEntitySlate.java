package at.petrak.hexcasting.common.blocks.circles;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.lib.HexBlockEntities;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockEntitySlate extends HexBlockEntity {
    public static final String TAG_PATTERN = "pattern";

    @Nullable
    public HexPattern pattern;

    public BlockEntitySlate(BlockPos pos, BlockState state) {
        super(HexBlockEntities.SLATE_TILE, pos, state);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (this.pattern != null) {
            components.set(HexDataComponents.PATTERN, this.pattern);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        var pat = componentInput.get(HexDataComponents.PATTERN);
        if (pat != null) {
            this.pattern = pat;
        }
    }

    @Override
    protected void saveModData(CompoundTag tag, HolderLookup.Provider registries) {
        if (this.pattern != null) {
            tag.put(TAG_PATTERN, HexPattern.CODEC.encodeStart(NbtOps.INSTANCE, pattern).getOrThrow());
        } else {
            tag.put(TAG_PATTERN, new CompoundTag());
        }
    }

    @Override
    protected void loadModData(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains(TAG_PATTERN, Tag.TAG_COMPOUND)) {
            Tag patternTag = tag.get(TAG_PATTERN);
            this.pattern = HexPattern.CODEC.parse(NbtOps.INSTANCE, patternTag).result().orElse(null);
        } else {
            this.pattern = null;
        }
    }

}
