package at.petrak.hexcasting.api.casting.math;

import at.petrak.hexcasting.api.casting.castables.Action;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.IntStream;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class HexSignature implements Iterable<HexAngle> {
    public static final int BITS_PER_TURN = 3;
    public static final int TURNS_PER_ARRAY_ELEMENT = Integer.SIZE / BITS_PER_TURN;

    private final int[] packedTurns;
    private final int memoizedHash;

    private HexSignature(int[] packedTurns) {
        this.packedTurns = packedTurns;
        this.memoizedHash = Arrays.hashCode(packedTurns);
    }

    private static final HexAngle[] BITS_TO_ANGLE = {
            null, HexAngle.getEntries().get(0), HexAngle.getEntries().get(1), HexAngle.getEntries().get(2),
            HexAngle.getEntries().get(3), HexAngle.getEntries().get(4), HexAngle.getEntries().get(5),
    };

    @Override
    public @NotNull Iterator<HexAngle> iterator() {
        return new Iterator<>() {
            private int arrayIndex = 0;
            private int turnIndex = 0;

            @Override
            public boolean hasNext() {
                return arrayIndex < packedTurns.length && (packedTurns[arrayIndex] & (0b111 << (turnIndex * BITS_PER_TURN))) != 0;
            }

            @Override
            public HexAngle next() {
                int bits = (packedTurns[arrayIndex] >> (turnIndex * BITS_PER_TURN)) & 0b111;
                turnIndex = (turnIndex + 1) % TURNS_PER_ARRAY_ELEMENT;
                if(turnIndex == 0) arrayIndex++;
                return BITS_TO_ANGLE[bits];
            }
        };
    }

    /**
     * @return null if this does not start with prefix, an Iterator with the remainder after the prefix otherwise
     */
    public @Nullable Iterator<HexAngle> stripPrefix(HexSignature prefix) {
        Iterator<HexAngle> strIt = this.iterator();
        Iterator<HexAngle> prefixIt = prefix.iterator();

        while(strIt.hasNext() && prefixIt.hasNext()) {
            if(!strIt.next().equals(prefixIt.next())) return null;
        }
        if(!prefixIt.hasNext()) return strIt;
        else return null;
    }

    public String toAnglesString() {
        StringBuilder builder = new StringBuilder(this.packedTurns.length * TURNS_PER_ARRAY_ELEMENT);
        this.iterator().forEachRemaining(a -> builder.append(a.toChar()));
        return builder.toString();
    }

    public int sizeUpperBound() {
        return this.packedTurns.length * TURNS_PER_ARRAY_ELEMENT;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof HexSignature sig) && Arrays.equals(this.packedTurns, sig.packedTurns);
    }

    @Override
    public int hashCode() {
        return this.memoizedHash;
    }

    @Override
    public String toString() {
        return "HexSignature[" + this.toAnglesString() + ']';
    }

    public static final Codec<HexSignature> CODEC = Codec.INT_STREAM
            .xmap(IntStream::toArray, Arrays::stream)
            .xmap(HexSignature::new, hs -> hs.packedTurns);

    public static final StreamCodec<ByteBuf, HexSignature> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public HexSignature decode(ByteBuf buf) {
            int length = VarInt.read(buf);
            int[] packedTurns = new int[length];
            int byteLength = length * Integer.BYTES;

            // zero-copy reinterpret as ints
            buf.nioBuffer(buf.readerIndex(), byteLength)
                    .asIntBuffer()
                    .get(packedTurns);

            buf.skipBytes(byteLength);

            return new HexSignature(packedTurns);
        }

        @Override
        public void encode(ByteBuf buf, HexSignature value) {
            VarInt.write(buf, value.packedTurns.length);

            int byteLength = value.packedTurns.length * Integer.BYTES;
            buf.ensureWritable(byteLength);

            // zero-copy reinterpret as ints
            buf.nioBuffer(buf.writerIndex(), byteLength)
                    .asIntBuffer()
                    .put(value.packedTurns);

            buf.writerIndex(buf.writerIndex() + byteLength);
        }
    };

    public static HexSignature fromAnglesStringUnchecked(String anglesString) {
        Builder b = new Builder(false);
        for(int i = 0; i < anglesString.length(); i++) {
            char c = anglesString.charAt(i);
            HexAngle a = HexAngle.fromChar(c);
            b.addAngle(a);
        }
        return b.build();
    }

    public static final class Builder {
        private final IntArrayList turnsBuilder;
        private int currentInt;
        private int currentTurnIndex;
        private boolean checked;
        private HexCoord cursor;
        private HexDir compass;
        private final LongOpenHashSet edges;

        public Builder() {
            this(true);
        }

        public Builder(boolean checked) {
            this.turnsBuilder = new IntArrayList();
            this.currentInt = 0;
            this.currentTurnIndex = 0;
            this.checked = checked;
            this.cursor = HexCoord.getOrigin();
            // orientation doesn't actually matter for edge checking
            this.compass = HexDir.EAST;
            this.edges = new LongOpenHashSet();

            if(this.checked) {
                this.edges.add(edgeRepresentation(cursor, cursor.plus(compass)));
            }
        }

        public void reset() {
            this.reset(true);
        }

        public void reset(boolean checked) {
            this.turnsBuilder.clear();
            this.currentInt = 0;
            this.currentTurnIndex = 0;
            this.checked = checked;
            this.cursor = HexCoord.getOrigin();
            this.compass = HexDir.EAST;
            this.edges.clear();

            if(this.checked) {
                this.edges.add(edgeRepresentation(this.cursor, this.cursor.plus(this.compass)));
            }
        }

        public int size() {
            return (10 * this.turnsBuilder.size()) + this.currentTurnIndex;
        }

        private static long edgeRepresentation(HexCoord from, HexCoord to) {
            short fromQ = (short) from.getQ();
            short fromR = (short) from.getR();
            short toQ = (short) to.getQ();
            short toR = (short) to.getR();

            int fromI = (fromQ << Short.SIZE) | (fromR & Short.MAX_VALUE);
            int toI = (toQ << Short.SIZE) | (toR & Short.MAX_VALUE);

            if(toI < fromI) {
                int tmp = fromI;
                fromI = toI;
                toI = tmp;
            }

            return ((long) fromI << Integer.SIZE) | ((long) toI & (long) Integer.MAX_VALUE);
        }

        public Builder addAngle(HexAngle angle) {
            if(this.checked) {
                HexCoord newCursor = this.cursor.plus(compass);
                HexDir newCompass = this.compass.times(angle);

                if (!this.edges.add(edgeRepresentation(newCursor, newCursor.plus(newCompass)))) {
                    int idx = this.size() - 1;
                    throw new IllegalStateException("Adding the angle %s at index %d made the pattern invalid by looping back on itself".formatted(angle, idx));
                }

                this.cursor = newCursor;
                this.compass = newCompass;
            }

            int turnBits = 1 + angle.ordinal();
            int maskIn = turnBits << (BITS_PER_TURN * this.currentTurnIndex);
            this.currentInt |= maskIn;

            this.currentTurnIndex = (this.currentTurnIndex + 1) % TURNS_PER_ARRAY_ELEMENT;
            // TODO doc this
            if(this.currentTurnIndex == 0) {
                this.turnsBuilder.add(this.currentInt);
                this.currentInt = 0;
            }

            return this;
        }

        public Builder undoAngle() {
            if(this.currentTurnIndex == 0) {
                this.currentInt = this.turnsBuilder.removeLast();
                this.currentTurnIndex = TURNS_PER_ARRAY_ELEMENT - 1;
            } else {
                this.currentTurnIndex = this.currentTurnIndex - 1;
            }

            int shift = this.currentTurnIndex * BITS_PER_TURN;
            int rawTurnBits = (this.currentInt >>> shift) & 0b111;

            HexAngle angle = BITS_TO_ANGLE[rawTurnBits];
            Objects.requireNonNull(angle);

            this.currentInt &= ~(0b111 << shift);

            if(this.checked) {
                this.edges.remove(edgeRepresentation(this.cursor, this.cursor.plus(this.compass)));

                HexAngle inverseAngle = angle.inverse();
                this.compass = this.compass.times(inverseAngle);

                this.cursor = this.cursor.plus(this.compass.times(HexAngle.BACK));
            }

            return this;
        }

        public HexSignature build() {
            if(this.currentInt != 0) this.turnsBuilder.add(this.currentInt);

            return new HexSignature(this.turnsBuilder.toIntArray());
        }
    }
}
