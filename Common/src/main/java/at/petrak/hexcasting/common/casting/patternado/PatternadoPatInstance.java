package at.petrak.hexcasting.common.casting.patternado;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;

import java.util.UUID;

public final class PatternadoPatInstance {
    private static final RandomSource RANDOM = RandomSource.create();

    private final HexPattern pattern;
    // Idx is used to determine where to draw it
    private final int idx;
    // -1 == infinite
    private int lifetime;
    // Use this so we can remove particular pats once a player finishes staffcasting
    private UUID uuid;

    public PatternadoPatInstance(HexPattern pattern, int lifetime, int idx) {
        this.pattern = pattern;
        this.lifetime = lifetime;
        this.idx = idx;
        this.uuid = UUID.randomUUID();
    }

    private PatternadoPatInstance(HexPattern pattern, int lifetime, int idx, UUID uuid) {
        this.pattern = pattern;
        this.lifetime = lifetime;
        this.idx = idx;
        this.uuid = uuid;
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

    public UUID getUuid() {
        return this.uuid;
    }

    public void tick() {
        if (this.lifetime > 0) {
            this.lifetime--;
        }
    }

    // we need to go through and make the ser/de names consistent
    public static PatternadoPatInstance loadFromWire(FriendlyByteBuf buf) {
        var pattern = HexPattern.fromNBT(buf.readNbt());
        var lifetime = buf.readVarInt();
        var idx = buf.readVarInt();
        var uuid = buf.readUUID();
        return new PatternadoPatInstance(pattern, lifetime, idx, uuid);
    }

    public void saveToWire(FriendlyByteBuf buf) {
        buf.writeNbt(this.pattern.serializeToNBT());
        buf.writeVarInt(this.lifetime);
        buf.writeInt(this.idx);
        buf.writeUUID(this.uuid);
    }
}
