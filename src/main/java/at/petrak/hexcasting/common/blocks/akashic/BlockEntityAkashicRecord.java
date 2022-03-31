package at.petrak.hexcasting.common.blocks.akashic;

import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.common.blocks.HexBlockTags;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import at.petrak.hexcasting.hexmath.HexDir;
import at.petrak.hexcasting.hexmath.HexPattern;
import at.petrak.paucal.api.PaucalBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.BiPredicate;

public class BlockEntityAkashicRecord extends PaucalBlockEntity {
    public static final String TAG_LOOKUP = "lookup",
        TAG_POS = "pos",
        TAG_DATUM = "datum",
        TAG_DIR = "dir";

    // Hex pattern signatures to pos and iota.
    // Note this is NOT a record of the entire floodfill! Just bookshelves.

    private final Map<String, Entry> entries = new HashMap<>();

    public BlockEntityAkashicRecord(BlockPos pWorldPosition, BlockState pBlockState) {
        super(HexBlocks.AKASHIC_RECORD_TILE.get(), pWorldPosition, pBlockState);
    }

    public void removeFloodfillerAt(BlockPos pos) {
        // lmao just recalc everything
        this.revalidateAllBookshelves();
    }

    /**
     * @return the block position of the place it gets stored, or null if there was no room.
     * <p>
     * Will never clobber anything.
     */
    public @Nullable BlockPos addNewDatum(HexPattern key, SpellDatum<?> datum) {
        if (this.entries.containsKey(key.anglesSignature())) {
            return null; // would clobber
        }

        var openPos = floodFillFor(this.worldPosition, this.level,
            (pos, bs) -> bs.is(HexBlockTags.AKASHIC_FLOODFILLER),
            (pos, bs) -> this.level.getBlockEntity(pos) instanceof BlockEntityAkashicBookshelf tile
                && tile.pattern == null);
        if (openPos != null) {
            var tile = (BlockEntityAkashicBookshelf) this.level.getBlockEntity(openPos);
            tile.recordPos = this.worldPosition;
            tile.pattern = key;
            tile.sync();

            this.entries.put(key.anglesSignature(), new Entry(openPos, key.startDir(), datum.serializeToNBT()));
            this.sync();
            return openPos;
        } else {
            return null;
        }
    }

    public @Nullable SpellDatum<?> lookupPattern(HexPattern key, ServerLevel slevel) {
        var entry = this.entries.get(key.anglesSignature());
        if (entry == null) {
            return null;
        } else {
            return SpellDatum.DeserializeFromNBT(entry.datum, slevel);
        }
    }

    public Component getDisplayAt(HexPattern key) {
        var entry = this.entries.get(key.anglesSignature());
        if (entry != null) {
            return SpellDatum.DisplayFromTag(entry.datum);
        } else {
            return new TranslatableComponent("hexcasting.spelldata.akashic.nopos").withStyle(ChatFormatting.RED);
        }
    }

    public int getCount() {
        return this.entries.size();
    }

    private void revalidateAllBookshelves() {
        // floodfill for all known positions
        var validPoses = new HashSet<BlockPos>();
        {
            var todo = new ArrayDeque<BlockPos>();
            todo.add(this.worldPosition);
            while (!todo.isEmpty()) {
                var here = todo.remove();

                for (var dir : Direction.values()) {
                    var neighbor = here.relative(dir);
                    if (validPoses.add(neighbor)) {
                        var bs = this.level.getBlockState(neighbor);
                        if (bs.is(HexBlockTags.AKASHIC_FLOODFILLER)) {
                            todo.add(neighbor);
                        }
                    }
                }
            }
        }

        var sigs = this.entries.keySet();
        for (var sig : sigs) {
            var entry = entries.get(sig);
            if (!validPoses.contains(entry.pos)) {
                // oh no!
                entries.remove(sig);
            }
        }

        this.sync();
    }


    @Override
    protected void saveModData(CompoundTag compoundTag) {
        var lookupTag = new CompoundTag();
        this.entries.forEach((sig, entry) -> {
            var t = new CompoundTag();
            t.put(TAG_POS, NbtUtils.writeBlockPos(entry.pos));
            t.put(TAG_DATUM, entry.datum);
            t.putByte(TAG_DIR, (byte) entry.startDir.ordinal());
            lookupTag.put(sig, t);
        });
        compoundTag.put(TAG_LOOKUP, lookupTag);
    }

    @Override
    protected void loadModData(CompoundTag compoundTag) {
        var lookupTag = compoundTag.getCompound(TAG_LOOKUP);

        var sigs = lookupTag.getAllKeys();
        for (var sig : sigs) {
            var entryTag = lookupTag.getCompound(sig);
            var pos = NbtUtils.readBlockPos(entryTag.getCompound(TAG_POS));
            var dir = HexDir.values()[entryTag.getByte(TAG_DIR)];
            var datum = entryTag.getCompound(TAG_DATUM);
            this.entries.put(sig, new Entry(pos, dir, datum));
        }
    }

    private record Entry(BlockPos pos, HexDir startDir, CompoundTag datum) {
    }

    public static @Nullable BlockPos floodFillFor(BlockPos start, Level world,
        BiPredicate<BlockPos, BlockState> isValid, BiPredicate<BlockPos, BlockState> isTarget) {
        var seenBlocks = new HashSet<BlockPos>();
        var todo = new ArrayDeque<BlockPos>();
        todo.add(start);

        while (!todo.isEmpty()) {
            var here = todo.remove();

            for (var dir : Direction.values()) {
                var neighbor = here.relative(dir);
                if (seenBlocks.add(neighbor)) {
                    var bs = world.getBlockState(neighbor);
                    if (isTarget.test(neighbor, bs)) {
                        return neighbor;
                    } else if (isValid.test(neighbor, bs)) {
                        todo.add(neighbor);
                    }
                }
            }
        }

        return null;
    }
}
