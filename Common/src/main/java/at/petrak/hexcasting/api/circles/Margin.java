package at.petrak.hexcasting.api.circles;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * A helper for {@link at.petrak.hexcasting.api.block.circle.BlockSidedCircleComponent BlockSidedCircleComponent}:
 * an abstraction over direction to only the 4 edges relevant to it.
 * <p>
 * When the normal dir is horizontal, {@code TOP} corresponds to {@code Direction.UP}, and {@code LEFT} and
 * {@code RIGHT} are to the left and to the right from the POV of <i>facing</i> that normal.
 * So, {@code Direction.NORTH}'s {@code RIGHT} is {@code Direction.WEST}.
 * <p>
 * When the normal dir is {@code Direction.UP} or {@code Direction.DOWN}, {@code TOP} corresponds to
 * {@code Direction.EAST}, cause we gotta pick something and +X seems reasonable. {@code Margin} always proceeds
 * counter-clockwise, so {@code Direction.UP}'s {@code RIGHT} is {@code Direction.EAST} and {@code Direction.DOWN}'s
 * {@code RIGHT} is {@code Direction.WEST}.
 * <p>
 * Things are additionally complicated because a {@code BlockSidedCircleComponent} can <i>also</i> have its TOP
 * dir facing any of the 4 margins, but that's a whole nother can of worms and I'm tired of typing doc comments.
 * Javac can yell a warning for not writing a doc comment, but it can't warn me for writing a <b>bad</b> one!
 */
public enum Margin implements StringRepresentable {
    TOP("top"),
    LEFT("left"),
    BOTTOM("bottom"),
    RIGHT("right");

    private final String serializedName;

    Margin(String serializedName) {
        this.serializedName = serializedName;
    }


    /**
     * Return which margin the given direction points as, given a normal vector.
     * <p>
     * Throws an exception if the two directions are on the same axis.
     */
    public static Margin fromNormalAndDir(Direction normal, Direction dir) throws IllegalArgumentException {
        var normalAxis = normal.getAxis();

        var cursor = switch (normalAxis) {
            case X, Z -> Direction.UP;
            case Y -> Direction.NORTH;
        };
        for (var margin : Margin.values()) {
            if (cursor == dir) {
                return margin;
            }
            cursor = normal.getAxisDirection() == Direction.AxisDirection.NEGATIVE
                ? cursor.getClockWise(normalAxis)
                : cursor.getCounterClockWise(normalAxis);
        }

        throw new IllegalArgumentException("Two directions with equal axes. Normal: " + normal + "; dir: " + dir);
    }

    /**
     * Convert this direction to the (absolute) edge given the normal vector of the block it's on.
     */
    public BlockEdge toEdge(Direction normal) {
        var topDir = switch (normal.getAxis()) {
            case X, Z -> Direction.UP;
            case Y -> Direction.NORTH;
        };
        var topEdge = BlockEdge.fromNormals(normal.getOpposite(), topDir);
        return topEdge.rotateAbout(normal, this.ordinal());
    }

    /**
     * If we consider {@code this} to be the "local {@code TOP}", transform {@code that} to face in the
     * appropriate direction.
     * <p>
     * Actually, I'm pretty sure this is transitive, so it doesn't matter which one you put as the receiver or argument.
     */
    public Margin transform(Margin that) {
        return Margin.values()[(this.ordinal() + that.ordinal()) % Margin.values().length];
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.serializedName;
    }
}
