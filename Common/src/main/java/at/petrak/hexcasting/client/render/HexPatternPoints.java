package at.petrak.hexcasting.client.render;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * static points making up a hex pattern to be rendered. It's used primarily for positioning, so we keep a
 * number of extra values here to avoid recomputing them.
 */
public class HexPatternPoints {
    public final ImmutableList<Vec2> zappyPoints;
    public final ImmutableList<Vec2> zappyPointsScaled;

    public final ImmutableList<Vec2> dotsScaled;

    public final double rangeX;
    public final double rangeY;
    public final double finalScale;

    public final double fullWidth;
    public final double fullHeight;

    private double minX = Double.MAX_VALUE;
    private double minY = Double.MAX_VALUE;

    private final double offsetX;
    private final double offsetY;

    private static final ConcurrentMap<String, HexPatternPoints> CACHED_STATIC_POINTS = new ConcurrentHashMap<>();

    private HexPatternPoints(HexPatternLike patternlike, PatternSettings patSets, double seed) {

        List<Vec2> dots = patternlike.getNonZappyPoints();

        // always do space calculations with the static version of the pattern
        // so that it doesn't jump around resizing itself.
        List<Vec2> zappyPoints = RenderLib.makeZappy(dots, patternlike.getDups(),
                patSets.getHops(), patSets.getVariance(), 0f, patSets.getFlowIrregular(),
                patSets.getReadabilityOffset(), patSets.getLastSegmentProp(), seed);


        this.zappyPoints = ImmutableList.copyOf(zappyPoints);
        double maxY = Double.MIN_VALUE;
        double maxX = Double.MIN_VALUE;
        for (Vec2 point : zappyPoints) {
            minX = Math.min(minX, point.x);
            maxX = Math.max(maxX, point.x);
            minY = Math.min(minY, point.y);
            maxY = Math.max(maxY, point.y);
        }
        rangeX = maxX - minX;
        rangeY = maxY - minY;

        // scales the patterns so that each point is patSets.baseScale units apart
        double baseScale = patSets.getBaseScale() / 1.5;

        // size of the pattern in pose space with no other adjustments
        double baseWidth = rangeX * baseScale;
        double baseHeight = rangeY * baseScale;

        // make sure that the scale fits within our min sizes
        double scale = Math.max(1.0, Math.max(
                (patSets.getMinWidth() - patSets.getStrokeWidthGuess()) / baseWidth,
                (patSets.getMinHeight() - patSets.getStrokeWidthGuess()) / baseHeight)
        );

        boolean vertFit = patSets.getVertAlignment().fit;
        boolean horFit = patSets.getHorAlignment().fit;

        // scale down if needed to fit in vertical space
        if(vertFit){
            scale = Math.min(scale, (patSets.getTargetHeight() - 2 * patSets.getVertPadding() - patSets.getStrokeWidthGuess())/(baseHeight));
        }

        // scale down if needed to fit in horizontal space
        if(horFit){
            scale = Math.min(scale, (patSets.getTargetWidth() - 2 * patSets.getHorPadding() - patSets.getStrokeWidthGuess())/(baseWidth));
        }

        finalScale = baseScale * scale;
        double finalStroke = patSets.getStrokeWidth(finalScale);

        double inherentWidth = (baseWidth * scale) + 2 * patSets.getHorPadding() + finalStroke;
        double inherentHeight = (baseHeight * scale) + 2 * patSets.getVertPadding() + finalStroke;

        // this is the amount of actual wiggle room we have for configurable position-ing.
        double widthDiff = Math.max(patSets.getTargetWidth() - inherentWidth, 0);
        double heightDiff = Math.max(patSets.getTargetHeight() - inherentHeight, 0);

        this.fullWidth = inherentWidth + widthDiff;
        this.fullHeight = inherentHeight + heightDiff;

        // center in inherent space and put extra space according to alignment stuff
        offsetX = ((inherentWidth - baseWidth * scale) / 2) + (widthDiff * patSets.getHorAlignment().amtInFront / 2);
        offsetY = ((inherentHeight - baseHeight * scale) / 2) + (heightDiff * patSets.getVertAlignment().amtInFront / 2);

        this.zappyPointsScaled = ImmutableList.copyOf(scaleVecs(zappyPoints));
        this.dotsScaled = ImmutableList.copyOf(scaleVecs(dots));
    }

    public Vec2 scaleVec(Vec2 point){
        return new Vec2(
                (float) (((point.x - this.minX) * this.finalScale) + this.offsetX),
                (float) (((point.y - this.minY) * this.finalScale) + this.offsetY)
        );
    }

    public List<Vec2> scaleVecs(List<Vec2> points){
        List<Vec2> scaledPoints = new ArrayList<>();
        for (Vec2 point : points) {
            scaledPoints.add(scaleVec(point));
        }
        return scaledPoints;
    }


    /**
     * Gets the static points for the given pattern, settings, and seed. This is cached.
     *
     * This is used in rendering static patterns and positioning non-static patterns.
     *
     */
    public static HexPatternPoints getStaticPoints(HexPatternLike patternlike, PatternSettings patSets, double seed){

        String cacheKey = patSets.getCacheKey(patternlike, seed);

        return CACHED_STATIC_POINTS.computeIfAbsent(cacheKey, (key) -> new HexPatternPoints(patternlike, patSets, seed) );
    }
}