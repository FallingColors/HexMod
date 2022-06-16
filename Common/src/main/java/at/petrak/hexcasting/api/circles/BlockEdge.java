package at.petrak.hexcasting.api.misc;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

/**
 * Edges are named after their <i>normal vectors</i>. For example, {@code XP_YP} is the top-east
 * corner of the block (positive x, positive y).
 */
public enum BlockEdge {
    XP_YP(Direction.EAST, Direction.UP),
    ZP_YP(Direction.UP, Direction.SOUTH),
    XN_YP(Direction.WEST, Direction.UP),
    ZN_YP(Direction.UP, Direction.NORTH),
    XP_ZP(Direction.EAST, Direction.SOUTH),
    XN_ZP(Direction.WEST, Direction.SOUTH),
    XP_ZN(Direction.EAST, Direction.NORTH),
    XN_ZN(Direction.WEST, Direction.NORTH),
    XP_YN(Direction.EAST, Direction.DOWN),
    ZP_YN(Direction.DOWN, Direction.SOUTH),
    XN_YN(Direction.WEST, Direction.DOWN),
    ZN_YN(Direction.DOWN, Direction.SOUTH);

    private final Direction norm1, norm2;

    BlockEdge(Direction norm1, Direction norm2) {
        this.norm1 = norm1;
        this.norm2 = norm2;
    }

    /**
     * Return the normals of this edge. They will be returned in order {@code X, Y, Z}.
     */
    public Pair<Direction, Direction> getNormals() {
        return Pair.of(this.norm1, this.norm2);
    }

    /**
     * Rotate this many quarters of a block counter-clockwise around the given axis.
     * "Counter-clockwise" is from the point of view of the axis, facing inwards. So, rotating once
     * about the {@code Y} axis brings {@code XP_ZP} to {@code XP_ZN}.
     */
    public BlockEdge rotateAbout(Direction.Axis axis, int quarters) {
        quarters = Mth.positiveModulo(quarters, 4);
        if (quarters == 0) {
            return this;
        }

        // Oh lord, group theory that minecraft has fortunately already done for me.
        Direction norm1$, norm2$;
        switch (quarters) {
            case 1 -> {
                norm1$ = this.norm1.getCounterClockWise(axis);
                norm2$ = this.norm2.getCounterClockWise(axis);
            }
            case 2 -> {
                norm1$ = this.norm1.getAxis() == axis ? this.norm1 : this.norm1.getOpposite();
                norm2$ = this.norm2.getAxis() == axis ? this.norm2 : this.norm2.getOpposite();
            }
            case 3 -> {
                norm1$ = this.norm1.getClockWise(axis);
                norm2$ = this.norm2.getClockWise(axis);
            }
            default -> throw new IllegalStateException();
        }

        return fromNormals(norm1$, norm2$);
    }

    /**
     * Rotate this many quarters of a block counter-clockwise around the given normal.
     * "Counter-clockwise" is from the point of view of the normal, facing outwards. So, rotating once
     * about the {@code NORTH} axis (negative Z) brings {@code XP_ZP} to {@code YP_ZP}.
     */
    public BlockEdge rotateAbout(Direction direction, int quarters) {
        if (quarters % 4 == 0) {
            return this;
        }
        return rotateAbout(direction.getAxis(),
            direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE ? -quarters : quarters);
    }

    /**
     * Get the edge from two normals.
     */
    public static BlockEdge fromNormals(Direction norm1, Direction norm2) {
        for (var edge : values()) {
            if (edge.norm1 == norm1 && edge.norm2 == norm2
                || edge.norm1 == norm2 && edge.norm2 == edge.norm1) {
                return edge;
            }
        }

        throw new IllegalStateException("Couldn't find " + norm1 + " & " + norm2);
    }
}
