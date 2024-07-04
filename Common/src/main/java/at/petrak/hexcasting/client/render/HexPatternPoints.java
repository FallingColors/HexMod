package at.petrak.hexcasting.client.render;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import net.minecraft.world.phys.Vec2;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class HexPatternPoints {
    public List<Vec2> zappyPoints;
//    public String pointsKey = null; //TODO: if a string key isnt performant enough override hashcode for points

    public double minX = Double.MAX_VALUE;
    public double maxX = Double.MIN_VALUE;
    public double minY = Double.MAX_VALUE;
    public double maxY = Double.MIN_VALUE;

    public double rangeX;
    public double rangeY;

    private static final HashMap<String, HexPatternPoints> CACHED_STATIC_POINTS = new HashMap<>();

    private HexPatternPoints(List<Vec2> zappyPoints) {
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

            return new HexPatternPoints(zappyPatternSpace);
        });
    }
}