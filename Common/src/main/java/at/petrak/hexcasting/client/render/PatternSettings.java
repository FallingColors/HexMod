package at.petrak.hexcasting.client.render;

import at.petrak.hexcasting.api.casting.math.HexPattern;

/**
 * A class holding settings for shaping and positioning patterns.
 *
 * By default, it's a simple wrapper of 3 records, however some use cases may require extending and overriding getters.
 * This is done to keep the complexity of the records a bit lower.
 */
public class PatternSettings {

    public PatternSettings(String name, PositionSettings posSets, StrokeSettings strokeSets, ZappySettings zapSets){
        this.name = name;
        this.posSets = posSets;
        this.strokeSets = strokeSets;
        this.zapSets = zapSets;
    }

    public record PositionSettings(double spaceWidth, double spaceHeight, double hPadding, double vPadding,
                                   AxisAlignment hAxis, AxisAlignment vAxis, double baseScale, double minWidth, double minHeight){
        /**
         * Makes settings ideal for rendering in a square. This helper exists because this is the most common positioning
         * pattern.
         * @param padding a value 0-0.5 for how much padding should go on each side.
         * @return a PositionSettings object in a 1x1 space with the given padding value such that the pattern is centered
         */
        public static PositionSettings paddedSquare(double padding){
            return paddedSquare(padding, 0.25, 0);
        }

        public static PositionSettings paddedSquare(double padding, double baseScale, double minSize){
            return new PositionSettings(1.0, 1.0, padding, padding, AxisAlignment.CENTER, AxisAlignment.CENTER, baseScale, minSize, minSize);
        }
    }

    public record StrokeSettings(double innerWidth, double outerWidth,
                                 double startDotRadius, double gridDotsRadius){
        public static StrokeSettings fromStroke(double stroke){
            return new StrokeSettings(stroke * 2.0/5.0, stroke, 0.8 * stroke * 2.0 / 5.0, 0.4 * stroke * 2.0 / 5.0);
        }
    }

    public record ZappySettings(int hops, float variance, float speed, float flowIrregular, float readabilityOffset, float lastSegmentLenProportion){
        public static float READABLE_OFFSET = 0.2f;
        public static float READABLE_SEGMENT = 0.8f;
        public static ZappySettings STATIC = new ZappySettings(10, 0.5f, 0f, 0.2f, 0, 1f);
        public static ZappySettings READABLE = new ZappySettings(10, 0.5f, 0f, 0.2f, READABLE_OFFSET, READABLE_SEGMENT);
        public static ZappySettings WOBBLY = new ZappySettings(10, 2.5f, 0.1f, 0.2f, 0, 1f);
    }

    public String getCacheKey(HexPattern pattern, double seed){
        return (pattern.getStartDir() + "-" + pattern.anglesSignature() + "-" + getName() + "-" + seed).toLowerCase();
    }

    // determines how the pattern is fit and aligned on a given axis
    public enum AxisAlignment{
        // aligns the pattern if there's extra space. these 3 are the same when the pattern needs to be scaled down to fit
        BEGIN,
        CENTER,
        END,
        // NONE ignores target space requirements when fitting on this axis.
        NONE
    }

    private final String name;
    private final PositionSettings posSets;
    private final StrokeSettings strokeSets;
    private final ZappySettings zapSets;

    public String getName(){ return name; }

    public double getTargetWidth(){ return posSets.spaceWidth; }
    public double getTargetHeight(){ return posSets.spaceHeight; }

    public double getHorPadding(){ return posSets.hPadding; }
    public double getVertPadding(){ return posSets.vPadding; }

    public AxisAlignment getHorAlignment(){ return posSets.hAxis; }
    public AxisAlignment getVertAlignment(){ return posSets.vAxis; }

    public double getBaseScale(){ return posSets.baseScale; }
    public double getMinWidth(){ return posSets.minWidth; }
    public double getMinHeight(){ return posSets.minHeight; }

    /* these sizing getters take in the final pattern scale so that patterns can vary their stroke width when squished.
     * the records keep a static value since that's fine for *most* use cases, override these methods if you need to use them.
     * note that these widths are still in pose space units.
     */

    public double getInnerWidth(double scale){ return strokeSets.innerWidth; }
    public double getOuterWidth(double scale){ return strokeSets.outerWidth; }

    public double getStartDotRadius(double scale){ return strokeSets.startDotRadius; }
    public double getGridDotsRadius(double scale){ return strokeSets.gridDotsRadius; }

    public double getStrokeWidth(double scale){ return Math.max(getOuterWidth(scale), getInnerWidth(scale)); }

    // we have a stroke guess getter so that we can *try* to account for the stroke size when fitting the pattern.
    public double getStrokeWidthGuess(){ return Math.max(strokeSets.outerWidth, strokeSets.innerWidth); }

    public int getHops(){ return zapSets.hops; }
    public float getVariance(){ return zapSets.variance; }
    public float getFlowIrregular(){ return zapSets.flowIrregular; }
    public float getReadabilityOffset(){ return zapSets.readabilityOffset; }
    public float getLastSegmentProp(){ return zapSets.lastSegmentLenProportion; }

    public float getSpeed(){ return zapSets.speed; }
}
