package at.petrak.hexcasting.common.blocks.akashic;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class BlockAkashicRecord extends Block {
    public BlockAkashicRecord(Properties p_49795_) {
        super(p_49795_);
    }


    /**
     * @return the block position of the place it gets stored, or null if there was no room.
     * <p>
     * Will clobber an existing shelf if the pattern matches.
     */
    public @Nullable
    BlockPos addNewDatum(BlockPos herePos, Level level, HexPattern key, Iota datum) {
        // look for an existing shelf with the provided keypattern
        var targetPos = AkashicFloodfiller.floodFillFor(herePos, level,
            (pos, bs, world) ->
                world.getBlockEntity(pos) instanceof BlockEntityAkashicBookshelf tile
                    && tile.getPattern() != null && tile.getPattern().getSignature().equals(key.getSignature()));
        // if there's no existing shelf that matches, look for an empty shelf
        if (targetPos == null) {
            targetPos = AkashicFloodfiller.floodFillFor(herePos, level, 0.9f,
                (pos, bs, world) ->
                    world.getBlockEntity(pos) instanceof BlockEntityAkashicBookshelf tile
                        && tile.getPattern() == null, 128);
        }
        // overwrite whatever shelf was found (may clobber existing data!)
        if (targetPos != null) {
            var tile = (BlockEntityAkashicBookshelf) level.getBlockEntity(targetPos);
            tile.setNewMapping(key, datum);
            return targetPos;
        } else {
            return null;
        }
    }

    public @Nullable Iota lookupPattern(BlockPos herePos, HexPattern key, ServerLevel slevel) {
        var foundPos = AkashicFloodfiller.floodFillFor(herePos, slevel,
            (pos, bs, world) ->
                world.getBlockEntity(pos) instanceof BlockEntityAkashicBookshelf tile
                    && tile.getPattern() != null && tile.getPattern().getSignature().equals(key.getSignature()));
        if (foundPos == null) {
            return null;
        }

        var tile = (BlockEntityAkashicBookshelf) slevel.getBlockEntity(foundPos);
        return tile != null ? tile.getIota() : null;
    }

    // TODO get comparators working again and also cache the number of iotas somehow?
}
