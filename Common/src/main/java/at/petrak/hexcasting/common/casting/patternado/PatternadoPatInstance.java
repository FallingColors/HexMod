package at.petrak.hexcasting.common.casting.patternado;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;

public final class PatternadoPatInstance {
    private static final RandomSource RANDOM = RandomSource.create();

    private static final String TAG_PATTERN = "pattern",
        TAG_IDX = "idx",
        TAG_SALT = "salt",
        TAG_LIFETIME = "lifetime";

    private final HexPattern pattern;
    // Idx is used to determine where to draw it; salt is just for rendering noise
    private final int idx, salt;
    private int lifetime;

    public PatternadoPatInstance(HexPattern pattern, int lifetime, int idx) {
        this.pattern = pattern;
        this.lifetime = lifetime;
        this.idx = idx;
        this.salt = RANDOM.nextInt();
    }

    private PatternadoPatInstance(HexPattern pattern, int lifetime, int idx, int salt) {
        this.pattern = pattern;
        this.lifetime = lifetime;
        this.idx = idx;
        this.salt = salt;
    }

    public HexPattern getPattern() {
        return pattern;
    }

    public int getLifetime() {
        return lifetime;
    }

    public int getIdx() {
        return this.idx;
    }

    public int getSalt() {
        return this.salt;
    }

    public void tick() {
        this.lifetime--;
    }

    // we need to go through and make the ser/de names consistent
    public static PatternadoPatInstance loadFromWire(FriendlyByteBuf buf) {
        var pattern = HexPattern.fromNBT(tag.getCompound(TAG_PATTERN));
        var lifetime = tag.getInt(TAG_LIFETIME);
        var idx = tag.getInt(TAG_IDX);
        var salt = tag.getInt(TAG_SALT);
        return new PatternadoPatInstance(pattern, lifetime, idx, salt);
    }

    public CompoundTag saveToNBT() {
        var out = new CompoundTag();
        out.put(TAG_PATTERN, this.pattern.serializeToNBT());
        out.putInt(TAG_LIFETIME, this.lifetime);
        out.putInt(TAG_IDX, this.idx);
        out.putInt(TAG_SALT, this.salt);
        return out;
    }


}
