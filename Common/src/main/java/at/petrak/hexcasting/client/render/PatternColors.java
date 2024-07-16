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

    protected final int startingDotColor;
    protected final int gridDotsColor;

    public PatternColors(int innerStartColor, int innerEndColor, int outerStartColor, int outerEndColor,
        int startingDotsColor, int gridDotsColor){
        this.innerStartColor = innerStartColor;
        this.innerEndColor = innerEndColor;
        this.outerStartColor = outerStartColor;
        this.outerEndColor = outerEndColor;
        this.startingDotColor = startingDotsColor;
        this.gridDotsColor = gridDotsColor;
    }

    // no gradient
    public PatternColors(int innerColor, int outerColor){
        this(innerColor, innerColor, outerColor, outerColor, 0, 0);
    }

    // single color -- no outer layer
    public PatternColors(int color){
        this(color, 0);
    }

    // add dots -- note, this is how you tell the renderer to make dots
    public PatternColors withDotColors(int startingDotColor, int gridDotsColor){
        return new PatternColors(this.innerStartColor, this.innerEndColor, this.outerStartColor, this.outerEndColor,
                startingDotColor, gridDotsColor);
    }
}
