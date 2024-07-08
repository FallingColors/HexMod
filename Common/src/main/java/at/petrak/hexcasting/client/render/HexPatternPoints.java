package at.petrak.hexcasting.client.render;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import net.minecraft.world.phys.Vec2;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * static points making up a hex pattern to be rendered. It's used primarily for positioning, so we keep a
 * number of extra values here to avoid recomputing them.
 */
public class HexPatternPoints {
    public List<Vec2> zappyPoints;
//    public String pointsKey = null; //TODO: if a string key isnt performant enough override hashcode for points

    public double minX = Double.MAX_VALUE;
    public double maxX = Double.MIN_VALUE;
    public double minY = Double.MAX_VALUE;
    public double maxY = Double.MIN_VALUE;

    public double rangeX;
    public double rangeY;

    public double offsetX;
    public double offsetY;

    public double baseScale;
    public double scale;
    public double finalScale;

    public double fullWidth;
    public double fullHeight;

    private static final ConcurrentMap<String, HexPatternPoints> CACHED_STATIC_POINTS = new ConcurrentHashMap<>();

    private HexPatternPoints(List<Vec2> zappyPoints, PatternRenderSettings patSets) {
        this.zappyPoints = zappyPoints;
//        pointsKey = PatternTextureManager.getPointsKey(zappyPoints);
        for (Vec2 point : zappyPoints) {
            minX = Math.min(minX, point.x);
            maxX = Math.max(maxX, point.x);
            minY = Math.min(minY, point.y);
            maxY = Math.max(maxY, point.y);
        }
        rangeX = maxX - minX;
        rangeY = maxY - minY;

        int patStepsX = (int)Math.round(rangeX / 1.5);
        int patStepsY = (int)Math.round(rangeY / 1.7);

        // scales the patterns so that each point is patSets.baseScale units apart
        baseScale = patSets.baseScale / 1.5;

        // size of the pattern in pose space with no other adjustments
        double baseWidth = rangeX * baseScale;
        double baseHeight = rangeY * baseScale;

        // make sure that the scale fits within our min sizes
        scale = Math.max(1.0, Math.max(patSets.minWidth / baseWidth, patSets.minHeight / baseHeight));


        // scale down if needed to fit in vertical space
        if(patSets.fitAxis.vertFit){
            scale = Math.min(scale, (patSets.spaceHeight - 2 * patSets.vPadding)/(baseHeight));
        }

        // scale down if needed to fit in horizontal space
        if(patSets.fitAxis.horFit){
            scale = Math.min(scale, (patSets.spaceWidth - 2 * patSets.hPadding)/(baseWidth));
        }

        finalScale = baseScale * scale;

        // either the space given or however long it goes if it's not fitted.
        fullWidth = (baseWidth * scale) + 2 * patSets.hPadding;
        fullHeight = (baseHeight * scale) + 2 * patSets.vPadding;

        if(patSets.fitAxis.horFit) fullWidth = Math.max(patSets.spaceWidth, fullWidth);
        if(patSets.fitAxis.vertFit) fullHeight = Math.max(patSets.spaceHeight, fullHeight);

        offsetX = (fullWidth - baseWidth * scale) / 2;
        offsetY = (fullHeight - baseHeight * scale) / 2;
    }


    /**
     * Gets the static points for the given pattern, settings, and seed. This is cached.
     *
     * This is used in rendering static patterns and positioning non-static patterns.
     *
     */
    public static HexPatternPoints getStaticPoints(HexPattern pattern, PatternRenderSettings patSets, double seed){

        String cacheKey = patSets.getCacheKey(pattern, seed);

        return CACHED_STATIC_POINTS.computeIfAbsent(cacheKey, (key) -> {
            List<Vec2> lines1 = pattern.toLines(1, Vec2.ZERO);
            Set<Integer> dupIndices = RenderLib.findDupIndices(pattern.positions());

            // always do space calculations with the static version of the pattern
            // so that it doesn't jump around resizing itself.
            List<Vec2> zappyPatternSpace = RenderLib.makeZappy(lines1, dupIndices,
                    patSets.hops, patSets.variance, 0f, patSets.flowIrregular, patSets.readabilityOffset, patSets.lastSegmentLenProportion, seed);

            return new HexPatternPoints(zappyPatternSpace, patSets);
        });
    }
}