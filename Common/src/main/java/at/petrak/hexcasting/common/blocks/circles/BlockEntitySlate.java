package at.petrak.hexcasting.common.blocks.circles;

import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import at.petrak.hexcasting.api.block.HexBlockEntity;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.lib.HexBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockEntitySlate extends HexBlockEntity implements ADIotaHolder {
    public static final String TAG_PATTERN = "pattern";

    @Nullable
    public HexPattern pattern;

    public BlockEntitySlate(BlockPos pos, BlockState state) {
        super(HexBlockEntities.SLATE_TILE, pos, state);
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
            if (HexPattern.isPattern(patternTag)) {
                this.pattern = HexPattern.fromNBT(patternTag);
            } else {
                this.pattern = null;
            }
        } else {
            this.pattern = null;
        }
    }

    @Override
    public CompoundTag readIotaTag(){
        if(pattern == null){
            return IotaType.serialize(emptyIota());
        }
        return IotaType.serialize(new PatternIota(pattern));
    }

    @Override
    public Iota readIota(ServerLevel world) {
        if(pattern == null){
            return emptyIota();
        }
        return new PatternIota(pattern);
    }

    @Override
    public boolean writeIota(@Nullable Iota iota, boolean simulate){
        if(!simulate){
            if(iota instanceof PatternIota pIota){
                this.pattern = pIota.getPattern();
                sync();
            }
            if(iota instanceof NullIota || iota == null){
                this.pattern = null;
                sync();
            }
        }
        return iota instanceof PatternIota || iota instanceof NullIota || iota == null;
    }

    @Override
    public boolean writeable(){
        return true;
    }
}
