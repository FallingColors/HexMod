package at.petrak.hexcasting.client.render;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec2;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class PatternTextureManager {

    //TODO: remove if not needed anymore for comparison
    public static boolean useTextures = true;
    public static int repaintIndex = 0;
    public static int resolutionScaler = 4;
    public static int fastRenderScaleFactor = 8; // e.g. this is 8, resolution is 1024, so render at 1024/8 = 128

    public static int resolutionByBlockSize = 128 * resolutionScaler;
    public static int paddingByBlockSize = 16 * resolutionScaler;
    public static int circleRadiusByBlockSize = 2 * resolutionScaler;
    public static int scaleLimit = 4 * resolutionScaler;
    public static int scrollLineWidth = 3 * resolutionScaler;
    public static int otherLineWidth = 4 * resolutionScaler;

    public static void setResolutionScaler(int resolutionScaler) {
        PatternTextureManager.resolutionScaler = resolutionScaler;
        resolutionByBlockSize = 128 * resolutionScaler;
        paddingByBlockSize = 16 * resolutionScaler;
        circleRadiusByBlockSize = 2 * resolutionScaler;
        scaleLimit = 4 * resolutionScaler;
        scrollLineWidth = 3 * resolutionScaler;
        otherLineWidth = 4 * resolutionScaler;
    }

    private static final ConcurrentMap<String, ResourceLocation> patternTexturesToAdd = new ConcurrentHashMap<>();
    // basically newCachedThreadPool, but with a max pool size
    private static final ExecutorService executor = new ThreadPoolExecutor(0, 16, 60L, TimeUnit.SECONDS, new LinkedBlockingDeque<>());


    private static final HashMap<String, ResourceLocation> patternTextures = new HashMap<>();

    public static String getPointsKey(List<Vec2> zappyPoints)
    {
        return zappyPoints.stream()
                .map(p -> String.format("(%f,%f)", p.x, p.y))
                .collect(Collectors.joining(";"));
    }

    public static Optional<ResourceLocation> getTexture(HexPattern pattern, PoseStack ps, PatternRenderSettings patSets, PatternColors patColors, double seed, boolean innerOrOuter) {
//    public static ResourceLocation getTexture(List<Vec2> points, String pointsKey, int blockSize, boolean showsStrokeOrder, float lineWidth, boolean useFullSize, Color innerColor, Color outerColor) {
        String patCacheKey = patSets.getCacheKey(pattern, seed) + (innerOrOuter ? "_inner" : "_outer");

        // move textures from concurrent map to normal hashmap as needed
        if (patternTexturesToAdd.containsKey(patCacheKey)) {
            var patternTexture = patternTexturesToAdd.remove(patCacheKey);
            var oldPatternTexture = patternTextures.put(patCacheKey, patternTexture);
            if (oldPatternTexture != null)
                Minecraft.getInstance().getTextureManager().getTexture(oldPatternTexture).close();

            return Optional.of(patternTexture);
        }
        if (patternTextures.containsKey(patCacheKey))
            return Optional.of(patternTextures.get(patCacheKey));

        // render a higher-resolution texture in a background thread so it eventually becomes all nice nice and pretty
        executor.submit(() -> {
            var slowTextures = createTextures(pattern, patSets, seed, false);

            // TextureManager#register doesn't look very thread-safe, so move back to the main thread after the slow part is done
            Minecraft.getInstance().execute(() -> {
                for(Map.Entry<String, DynamicTexture> textureEntry : slowTextures.entrySet()){
                    registerTexture(patCacheKey + "_" + textureEntry.getKey(), textureEntry.getValue(), true);
                }
            });
        });
        return Optional.empty();
    }

    private static Map<String, DynamicTexture> createTextures(HexPattern pattern, PatternRenderSettings patSets, double seed, boolean fastRender)
//    private static DynamicTexture createTexture(List<Vec2> points, int blockSize, boolean showsStrokeOrder, float lineWidth, boolean useFullSize, Color innerColor, Color outerColor, boolean fastRender)
    {
//        int resolution = resolutionByBlockSize * blockSize;
//        int padding = paddingByBlockSize * blockSize;

        int resolution = resolutionByBlockSize;
        int padding = paddingByBlockSize;

        if (fastRender) {
            resolution /= fastRenderScaleFactor;
            padding /= fastRenderScaleFactor;
//            lineWidth /= (float)fastRenderScaleFactor;
        }

        HexPatternPoints staticPoints = HexPatternPoints.getStaticPoints(pattern, patSets, seed);

        double baseScale = patSets.baseScale / 1.5;

        // size of the pattern in pose space with no other adjustments
        double baseWidth = staticPoints.rangeX * baseScale;
        double baseHeight = staticPoints.rangeY * baseScale;

        // make sure that the scale fits within our min sizes
        double scale = Math.max(1.0, Math.max(patSets.minWidth / baseWidth, patSets.minHeight / baseHeight));

        // scale down if needed to fit in vertical space
        if(patSets.fitAxis.vertFit){
            scale = Math.min(scale, (patSets.spaceHeight - 2 * patSets.vPadding)/(baseHeight));
        }

        // scale down if needed to fit in horizontal space
        if(patSets.fitAxis.horFit){
            scale = Math.min(scale, (patSets.spaceWidth - 2 * patSets.hPadding)/(baseWidth));
        }

        List<Vec2> zappyRenderSpace = new ArrayList<>();

        for (Vec2 point : staticPoints.zappyPoints) {
            zappyRenderSpace.add(new Vec2(
                    (float) (((point.x - staticPoints.minX) * baseScale * scale) + patSets.hPadding),
                    (float) (((point.y - staticPoints.minY) * baseScale * scale) + patSets.vPadding)
            ));
        }

//
//        double offsetX = ((resolution - 2 * padding) - rangeX * scale) / 2;
//        double offsetY = ((resolution - 2 * padding) - rangeY * scale) / 2;

        BufferedImage img = new BufferedImage(resolution, resolution, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

//        g2d.setColor(outerColor);
//        g2d.setStroke(new BasicStroke((blockSize * 5f / 3f) * lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
//        drawLines(g2d, points, minX, minY, scale, offsetX, offsetY, padding);
//
//        g2d.setColor(innerColor);
//        g2d.setStroke(new BasicStroke((blockSize * 2f / 3f) * lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
//        drawLines(g2d, points, minX, minY, scale, offsetX, offsetY, padding);
//
//
//        if (showsStrokeOrder) {
//            g2d.setColor(new Color(0xff_d77b5b));
//            Tuple<Integer, Integer> point = getTextureCoordinates(points.get(0), minX, minY, scale, offsetX, offsetY, padding);
//            int spotRadius = circleRadiusByBlockSize * blockSize;
//            drawHexagon(g2d, point.getA(), point.getB(), spotRadius);
//        }

        g2d.dispose();

        NativeImage nativeImage = new NativeImage(img.getWidth(), img.getHeight(), true);
        for (int y = 0; y < img.getHeight(); y++)
            for (int x = 0; x < img.getWidth(); x++)
                nativeImage.setPixelRGBA(x, y, img.getRGB(x, y));

        return new HashMap<>(); // temporary
    }

    private static ResourceLocation registerTexture(String patTextureKey, DynamicTexture dynamicTexture, boolean isSlow) {
        // isSlow used to register different textures for the low-resolution, fastly rendered version of each texture
        // and the high-resolution, slowly rendered version (this means the slow doesn't replace the fast in the texture manager,
        // which causes occasional visual stuttering for a frame).
        String name = "hex_pattern_texture_" + patTextureKey + "_" + repaintIndex + "_" + (isSlow ? "slow" : "fast") + ".png";
        ResourceLocation resourceLocation = Minecraft.getInstance().getTextureManager().register(name, dynamicTexture);
        patternTexturesToAdd.put(patTextureKey, resourceLocation);
        return resourceLocation;
    }

    private static void drawLines(Graphics2D g2d, List<Vec2> points, double minX, double minY, double scale, double offsetX, double offsetY, int padding) {
        for (int i = 0; i < points.size() - 1; i++) {
            Tuple<Integer, Integer> pointFrom = getTextureCoordinates(points.get(i), minX, minY, scale, offsetX, offsetY, padding);
            Tuple<Integer, Integer> pointTo = getTextureCoordinates(points.get(i+1), minX, minY, scale, offsetX, offsetY, padding);
            g2d.drawLine(pointFrom.getA(), pointFrom.getB(), pointTo.getA(), pointTo.getB());
        }
    }

    private static Tuple<Integer, Integer> getTextureCoordinates(Vec2 point, double minX, double minY, double scale, double offsetX, double offsetY, int padding) {
        int x = (int) ((point.x - minX) * scale + offsetX) + padding;
        int y = (int) ((point.y - minY) * scale + offsetY) + padding;
        return new Tuple<>(x, y);
    }

    private static void drawHexagon(Graphics2D g2d, int x, int y, int radius) {
        int fracOfCircle = 6;
        Polygon hexagon = new Polygon();

        for (int i = 0; i < fracOfCircle; i++) {
            double theta = (i / (double) fracOfCircle) * Math.PI * 2;
            int hx = (int) (x + Math.cos(theta) * radius);
            int hy = (int) (y + Math.sin(theta) * radius);
            hexagon.addPoint(hx, hy);
        }

        g2d.fill(hexagon);
    }

    public static void repaint() {
        repaintIndex++;
        patternTexturesToAdd.clear();
        patternTextures.clear();
    }
}