package at.petrak.hexcasting.client.render;

/**
 * An immutable wrapper for pattern colors.
 * <p>
 * This is separate from PatternRenderSettings because it does not affect the shape of the pattern, so we can re-use
 * those parts for different colors.
 */
public record PatternColors(int innerStartColor, int innerEndColor, int outerStartColor, int outerEndColor,
                            int startingDotColor, int gridDotsColor){

    // keep some handy frequently used colors here.
    public static final PatternColors DEFAULT_PATTERN_COLOR = new PatternColors(0xff_554d54, 0xff_d2c8c8);
    public static final PatternColors DIMMED_COLOR = new PatternColors(0xFF_B4AAAA, 0xff_d2c8c8);
    public static final PatternColors DEFAULT_GRADIENT_COLOR = DEFAULT_PATTERN_COLOR.withGradientEnds(DIMMED_COLOR);

    public static final int STARTING_DOT = 0xff_5b7bd7;
    public static final int GRID_DOTS = 0x80_d2c8c8;

    public static final PatternColors READABLE_SCROLL_COLORS = DEFAULT_PATTERN_COLOR.withDots(true, false);
    public static final PatternColors READABLE_GRID_SCROLL_COLORS = DEFAULT_PATTERN_COLOR.withDots(true, true);

    public static final PatternColors SLATE_WOBBLY_COLOR = glowyStroke( 0xff_64c8ff); // old blue color
    public static final PatternColors SLATE_WOBBLY_PURPLE_COLOR = glowyStroke(0xff_cfa0f3); // shiny new purple one :)

    // no gradient
    public PatternColors(int innerColor, int outerColor){
        this(innerColor, innerColor, outerColor, outerColor, 0, 0);
    }

    // single color -- no inner layer
    public static PatternColors singleStroke(int color){
        return new PatternColors(0, color);
    }

    // makes a stroke color similar to the glowy effect that slates have.
    public static PatternColors glowyStroke(int color){
        return new PatternColors(RenderLib.screenCol(color), color);
    }

    public static PatternColors gradientStrokes(int innerStartColor, int innerEndColor, int outerStartColor, int outerEndColor){
        return new PatternColors(innerStartColor, innerEndColor, outerStartColor, outerEndColor, 0, 0);
    }

    // a single stroke with a gradient -- no inner layer.
    public static PatternColors gradientStroke(int startColor, int endColor){
        return PatternColors.gradientStrokes(0, 0, startColor, endColor);
    }

    public PatternColors withGradientEnds(int endColorInner, int endColorOuter){
        return new PatternColors(this.innerStartColor, endColorInner, this.outerStartColor, endColorOuter, this.startingDotColor, this.gridDotsColor);
    }

    public PatternColors withGradientEnds(PatternColors end){
        return withGradientEnds(end.innerEndColor, end.outerEndColor);
    }

    // add dots -- note, this is how you tell the renderer to make dots
    public PatternColors withDotColors(int startingDotColor, int gridDotsColor){
        return new PatternColors(this.innerStartColor, this.innerEndColor, this.outerStartColor, this.outerEndColor,
                startingDotColor, gridDotsColor);
    }

    // adds dots with the default colors.
    public PatternColors withDots(boolean startingDot, boolean gridDots){
        return withDotColors(startingDot ? STARTING_DOT : 0, gridDots ? GRID_DOTS : 0);
    }
}
