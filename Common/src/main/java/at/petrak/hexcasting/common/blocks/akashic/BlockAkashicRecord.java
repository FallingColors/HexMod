package at.petrak.hexcasting.common.blocks.akashic;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
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
     * Will never clobber anything.
     */
    public @Nullable
    BlockPos addNewDatum(BlockPos herePos, Level level, HexPattern key, Iota datum) {
        var clobbereePos = AkashicFloodfiller.floodFillFor(herePos, level,
            (pos, bs, world) ->
                world.getBlockEntity(pos) instanceof BlockEntityAkashicBookshelf tile
                    && tile.getPattern() != null && tile.getPattern().sigsEqual(key));

        if (clobbereePos != null) {
            return null;
        }

        var openPos = AkashicFloodfiller.floodFillFor(herePos, level, 0.9f,
            (pos, bs, world) ->
                world.getBlockEntity(pos) instanceof BlockEntityAkashicBookshelf tile
                    && tile.getPattern() == null, 128);
        if (openPos != null) {
            var tile = (BlockEntityAkashicBookshelf) level.getBlockEntity(openPos);
            tile.setNewMapping(key, datum);

            return openPos;
        } else {
            return null;
        }
    }

    public @Nullable
    Iota lookupPattern(BlockPos herePos, HexPattern key, ServerLevel slevel) {
        var foundPos = AkashicFloodfiller.floodFillFor(herePos, slevel,
            (pos, bs, world) ->
                world.getBlockEntity(pos) instanceof BlockEntityAkashicBookshelf tile
                    && tile.getPattern() != null && tile.getPattern().sigsEqual(key));
        if (foundPos == null) {
            return null;
        }

        var tile = (BlockEntityAkashicBookshelf) slevel.getBlockEntity(foundPos);
        var tag = tile.getIotaTag();
        return tag == null ? null : IotaType.deserialize(tag, slevel);
    }

    // TODO get comparators working again and also cache the number of iotas somehow?
}
