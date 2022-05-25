package at.petrak.hexcasting.common.blocks.akashic;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import at.petrak.hexcasting.api.spell.DatumType;
import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.api.spell.math.HexDir;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.common.lib.HexBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BlockEntityAkashicRecord extends HexBlockEntity {
    public static final String TAG_LOOKUP = "lookup",
        TAG_POS = "pos",
        TAG_DATUM = "datum",
        TAG_DIR = "dir";

    // Hex pattern signatures to pos and iota.
    // Note this is NOT a record of the entire floodfill! Just bookshelves.

    private final Map<String, Entry> entries = new HashMap<>();

    public BlockEntityAkashicRecord(BlockPos pWorldPosition, BlockState pBlockState) {
        super(HexBlockEntities.AKASHIC_RECORD_TILE, pWorldPosition, pBlockState);
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
    public @Nullable
    BlockPos addNewDatum(HexPattern key, SpellDatum<?> datum) {
        String entryKey = getKey(key);
        if (this.entries.containsKey(entryKey)) {
            return null; // would clobber
        }

        var openPos = BlockAkashicFloodfiller.floodFillFor(this.worldPosition, this.level,
            (pos, bs, world) -> world.getBlockEntity(pos) instanceof BlockEntityAkashicBookshelf tile
                && tile.getPattern() == null);
        if (openPos != null) {
            var tile = (BlockEntityAkashicBookshelf) this.level.getBlockEntity(openPos);
            tile.setNewData(this.getBlockPos(), key, datum.getType());

            this.entries.put(entryKey, new Entry(openPos, key.getStartDir(), datum.serializeToNBT()));
            this.sync();

            return openPos;
        } else {
            return null;
        }
    }

    private String getKey(HexPattern key) {
        String angles = key.anglesSignature();
        if (angles.isEmpty()) {
            return "empty"; // contains non-angle characters, so can't occur any way other than this
        }
        return angles;
    }

    public @Nullable
    SpellDatum<?> lookupPattern(HexPattern key, ServerLevel slevel) {
        var entry = this.entries.get(getKey(key));
        if (entry == null) {
            return null;
        } else {
            return SpellDatum.fromNBT(entry.datum, slevel);
        }
    }

    public Component getDisplayAt(HexPattern key) {
        var entry = this.entries.get(getKey(key));
        if (entry != null) {
            return SpellDatum.displayFromNBT(entry.datum);
        } else {
            return new TranslatableComponent("hexcasting.spelldata.akashic.nopos").withStyle(ChatFormatting.RED);
        }
    }

    public int getCount() {
        return this.entries.size();
    }

    public void revalidateAllBookshelves() {
        // floodfill for all known positions
        var validPoses = new HashSet<BlockPos>();
        {
            var seen = new HashSet<BlockPos>();
            var todo = new ArrayDeque<BlockPos>();
            todo.add(this.worldPosition);
            // we do NOT add this position to the valid positions, because the record
            // isn't flood-fillable through.
            while (!todo.isEmpty()) {
                var here = todo.remove();

                for (var dir : Direction.values()) {
                    var neighbor = here.relative(dir);
                    if (seen.add(neighbor)) {
                        var bs = this.level.getBlockState(neighbor);
                        if (BlockAkashicFloodfiller.canItBeFloodedThrough(neighbor, bs, this.level)) {
                            todo.add(neighbor);
                            if (this.level.getBlockEntity(neighbor) instanceof BlockEntityAkashicBookshelf &&
                                bs.hasProperty(BlockAkashicBookshelf.DATUM_TYPE) &&
                                bs.getValue(BlockAkashicBookshelf.DATUM_TYPE) != DatumType.EMPTY) {
                                validPoses.add(neighbor);
                            }
                        }
                    }
                }
            }
        }

        var sigs = new ArrayList<>(this.entries.keySet());
        for (var sig : sigs) {
            var entry = this.entries.get(sig);
            if (!validPoses.contains(entry.pos)) {
                // oh no!
                this.entries.remove(sig);

                if (this.level.getBlockEntity(entry.pos) instanceof BlockEntityAkashicBookshelf shelf) {
                    shelf.setNewData(null, null, DatumType.EMPTY);
                }
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

        this.entries.clear();
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

}
