package at.petrak.hexcasting.client.render;

/**
 * An immutable wrapper for pattern colors.
 *
 * This is separate from PatternRenderSettings because it does not affect the shape of the pattern, so we can re-use
 * those parts for different colors.
 */
public class PatternColors {
    protected final int innerStartColor;
    protected final int innerEndColor;
    protected final int outerStartColor;
    protected final int outerEndColor;

    public PatternColors(int innerStartColor, int innerEndColor, int outerStartColor, int outerEndColor){
        this.innerStartColor = innerStartColor;
        this.innerEndColor = innerEndColor;
        this.outerStartColor = outerStartColor;
        this.outerEndColor = outerEndColor;
    }

    public PatternColors(int innerColor, int outerColor){
        this(innerColor, innerColor, outerColor, outerColor);
    }

    // single color
    public PatternColors(int color){
        this(color, 0);
    }
}
