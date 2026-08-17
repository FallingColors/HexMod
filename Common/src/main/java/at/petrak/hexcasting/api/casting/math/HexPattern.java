package at.petrak.hexcasting.api.casting.math;

import at.petrak.hexcasting.api.utils.HexUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec2;

/**
 * Sequence of angles to define a pattern traced.
 */
public final class HexPattern {
    private final HexDir orientation;
    private final HexSignature signature;

    public HexPattern(HexDir orientation, HexSignature signature) {
        this.orientation = orientation;
        this.signature = signature;
    }

    public HexDir getOrientation() {
        return orientation;
    }

    public HexSignature getSignature() {
        return signature;
    }

    public Vec2 getCenter(float hexRadius) {
        return this.getCenter(hexRadius, HexCoord.getOrigin());
    }

    public Vec2 getCenter(float hexRadius, HexCoord origin) {
        Vec2 originPx = HexUtils.coordToPx(origin, hexRadius, Vec2.ZERO);
        List<Vec2> points = this.toLines(hexRadius, originPx);
        return HexUtils.findCenter(points);
    }

    /**
     * Convert a hex pattern into a sequence of straight linePoints spanning its points.
     */
    public List<Vec2> toLines(float hexSize, Vec2 origin) {
        return this.positions().stream().map(it -> HexUtils.coordToPx(it, hexSize, origin)).toList();
    }

    public List<HexCoord> positions() {
        return this.positions(HexCoord.getOrigin());
    }

    public List<HexCoord> positions(HexCoord start)  {
        ArrayList<HexCoord> out = new ArrayList<>(this.signature.sizeUpperBound() + 2);
        out.add(start);
        HexDir compass = this.orientation;
        HexCoord cursor = start;
        for (HexAngle a : this.signature) {
            cursor = cursor.plus(compass);
            out.add(cursor);
            compass = compass.times(a);
        }
        out.add(cursor.plus(compass));
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        HexPattern that = (HexPattern) o;
        return Objects.equals(signature, that.signature) && orientation == that.orientation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(signature, orientation);
    }

    @Override
    public String toString() {
        return "HexPattern[" +
                "orientation=" + orientation +
                ", signature=" + signature +
                ']';
    }

    public String toChatString() {
        return "HexPattern[" + orientation + ", " + signature.toAnglesString() + ']';
    }

    public static final Codec<HexPattern> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            HexDir.CODEC.fieldOf("orientation").forGetter(HexPattern::getOrientation),
            HexSignature.CODEC.fieldOf("signature").forGetter(HexPattern::getSignature)
    ).apply(instance, HexPattern::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, HexPattern> STREAM_CODEC = StreamCodec.composite(
            HexDir.STREAM_CODEC, HexPattern::getOrientation,
            HexSignature.STREAM_CODEC, HexPattern::getSignature,
            HexPattern::new
    );

    public static HexPattern fromAngleString(String angleString, HexDir startDir) {
        return fromAngleString(angleString, startDir, true);
    }

    public static HexPattern fromAngleString(String angleString, HexDir startDir, boolean rejectOverlap) {
        HexSignature.Builder signature = new HexSignature.Builder(rejectOverlap);

        for (int i = 0; i < angleString.length(); i++) {
            char c = angleString.charAt(i);

            HexAngle angle = HexAngle.fromChar(c);
            if(angle == null) throw new IllegalArgumentException("Cannot match %c at idx %d to a direction".formatted(c, i));

            signature.addAngle(angle);
        }

        return new HexPattern(startDir, signature.build());
    }
}
