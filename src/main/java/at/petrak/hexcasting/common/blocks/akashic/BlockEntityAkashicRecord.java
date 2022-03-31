package at.petrak.hexcasting.common.blocks.akashic;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.common.blocks.HexBlockTags;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import at.petrak.hexcasting.hexmath.HexDir;
import at.petrak.hexcasting.hexmath.HexPattern;
import at.petrak.paucal.api.PaucalBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
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

    // When deserializing on the server we have the actual iotae...
    private Map<String, ServerEntry> serverEntries;
    // on the client we just have their displays
    private Map<String, ClientEntry> clientEntries;

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
        if (this.serverEntries == null) {
            HexMod.getLogger().warn("should only call addNewDatum on the server");
            return null;
        }
        if (this.serverEntries.containsKey(key.anglesSignature())) {
            return null; // would clobber
        }

        var openPos = floodFillFor(this.worldPosition, this.level,
            (pos, bs) -> bs.is(HexBlockTags.AKASHIC_FLOODFILLER),
            (pos, bs) -> this.level.getBlockEntity(pos) instanceof BlockEntityAkashicBookshelf tile
                && tile.recordPos == null);
        if (openPos != null) {
            var tile = (BlockEntityAkashicBookshelf) this.level.getBlockEntity(openPos);
            tile.recordPos = this.worldPosition;
            tile.pattern = key;
            tile.setChanged();

            this.serverEntries.put(key.anglesSignature(), new ServerEntry(openPos, key.startDir(), datum));
            this.setChanged();
            return openPos;
        } else {
            return null;
        }
    }

    public @Nullable SpellDatum<?> lookupPattern(HexPattern key) {
        if (this.serverEntries == null) {
            HexMod.getLogger().warn("should only call lookupPattern on the server");
            return null;
        }

        var entry = this.serverEntries.get(key.anglesSignature());
        if (entry == null) {
            return null;
        } else {
            return entry.datum;
        }
    }

    public Component getDisplayAt(HexPattern key) {
        if (this.clientEntries == null) {
            HexMod.getLogger().warn("should only call getDisplayAt on the client");
            return new TextComponent("");
        }

        var entry = this.clientEntries.get(key.anglesSignature());
        if (entry != null) {
            return entry.display;
        } else {
            return new TranslatableComponent("hexcasting.spelldata.akashic.nopos").withStyle(ChatFormatting.RED);
        }
    }

    public int getCount() {
        var lookup = this.getLookup();
        return lookup.size();
    }

    public void reifyLookupsPerSide() {
        if (this.level.isClientSide) {
            if (this.clientEntries == null) {
                this.clientEntries = new HashMap<>();
                this.setChanged();
            }
        } else {
            if (this.serverEntries == null) {
                this.serverEntries = new HashMap<>();
                this.setChanged();
            }
        }

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

        var lookup = this.getLookup();
        var sigs = lookup.keySet();
        for (var sig : sigs) {
            var entry = lookup.get(sig);
            if (!validPoses.contains(entry.getPos())) {
                // oh no!
                lookup.remove(sig);
            }
        }

        this.setChanged();
    }

    private Map<String, ? extends PosAndDirHaver> getLookup() {
        if (this.serverEntries != null) {
            return this.serverEntries;
        } else if (this.clientEntries != null) {
            return this.clientEntries;
        } else {
            // uh oh
            throw new IllegalStateException("both server and client entries were null");
        }
    }

    @Override
    protected void saveModData(CompoundTag compoundTag) {
        var lookupTag = new CompoundTag();
        if (this.clientEntries != null) {
            this.clientEntries.forEach((sig, entry) -> {
                var t = new CompoundTag();
                t.put(TAG_POS, NbtUtils.writeBlockPos(entry.pos));
                t.put(TAG_DATUM, entry.original);
                t.putByte(TAG_DIR, (byte) entry.startDir.ordinal());
                lookupTag.put(sig, t);
            });
        } else if (this.serverEntries != null) {
            this.serverEntries.forEach((sig, entry) -> {
                var t = new CompoundTag();
                t.put(TAG_POS, NbtUtils.writeBlockPos(entry.pos));
                t.put(TAG_DATUM, entry.datum.serializeToNBT());
                t.putByte(TAG_DIR, (byte) entry.startDir.ordinal());
                lookupTag.put(sig, t);
            });
        }
        compoundTag.put(TAG_LOOKUP, lookupTag);
    }

    @Override
    protected void loadModData(CompoundTag compoundTag) {
        var lookupTag = compoundTag.getCompound(TAG_LOOKUP);
        var sigs = lookupTag.getAllKeys();

        if (this.level instanceof ClientLevel) {
            this.clientEntries = new HashMap<>();
            for (var sig : sigs) {
                var entryTag = lookupTag.getCompound(sig);
                var pos = NbtUtils.readBlockPos(entryTag.getCompound(TAG_POS));
                var dir = HexDir.values()[entryTag.getByte(TAG_DIR)];
                var display = SpellDatum.DisplayFromTag(entryTag.getCompound(TAG_DATUM));
                this.clientEntries.put(sig, new ClientEntry(pos, dir, display, entryTag));
            }
        } else if (this.level instanceof ServerLevel sworld) {
            this.serverEntries = new HashMap<>();
            for (var sig : sigs) {
                var entryTag = lookupTag.getCompound(sig);
                var pos = NbtUtils.readBlockPos(entryTag.getCompound(TAG_POS));
                var dir = HexDir.values()[entryTag.getByte(TAG_DIR)];
                var datum = SpellDatum.DeserializeFromNBT(entryTag.getCompound(TAG_DATUM), sworld);
                this.serverEntries.put(sig, new ServerEntry(pos, dir, datum));
            }
        }
    }

    private interface PosAndDirHaver {
        BlockPos getPos();

        HexDir getDir();
    }

    private record ServerEntry(BlockPos pos, HexDir startDir, SpellDatum<?> datum) implements PosAndDirHaver {
        @Override
        public BlockPos getPos() {
            return this.pos;
        }

        @Override
        public HexDir getDir() {
            return this.startDir;
        }
    }

    private record ClientEntry(BlockPos pos, HexDir startDir, Component display,
                               CompoundTag original) implements PosAndDirHaver {
        @Override
        public BlockPos getPos() {
            return this.pos;
        }

        @Override
        public HexDir getDir() {
            return this.startDir;
        }
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
