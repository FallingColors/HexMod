package at.petrak.hexcasting.client.render;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import com.mojang.blaze3d.platform.NativeImage;
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

    private static final ConcurrentMap<String, Map<String, ResourceLocation>> patternTexturesToAdd = new ConcurrentHashMap<>();
    private static final Set<String> inProgressPatterns = new HashSet<>();
    // basically newCachedThreadPool, but with a max pool size
    private static final ExecutorService executor = new ThreadPoolExecutor(0, 16, 60L, TimeUnit.SECONDS, new LinkedBlockingDeque<>());


    private static final HashMap<String, Map<String, ResourceLocation>> patternTextures = new HashMap<>();

    public static Optional<Map<String, ResourceLocation>> getTextures(HexPattern pattern, PatternRenderSettings patSets, double seed, int resPerUnit) {
        String patCacheKey = patSets.getCacheKey(pattern, seed) + "_" + resPerUnit;

        // move textures from concurrent map to normal hashmap as needed
        if (patternTexturesToAdd.containsKey(patCacheKey)) {
            var patternTexture = patternTexturesToAdd.remove(patCacheKey);
            var oldPatternTexture = patternTextures.put(patCacheKey, patternTexture);
            inProgressPatterns.remove(patCacheKey);
            if (oldPatternTexture != null) // TODO: is this needed? when does this ever happen?
                for(ResourceLocation oldPatternTextureSingle : oldPatternTexture.values())
                    Minecraft.getInstance().getTextureManager().getTexture(oldPatternTextureSingle).close();

            return Optional.of(patternTexture);
        }
        if (patternTextures.containsKey(patCacheKey))
            return Optional.of(patternTextures.get(patCacheKey));

        // render a higher-resolution texture in a background thread so it eventually becomes all nice nice and pretty
        if(!inProgressPatterns.contains(patCacheKey)){
            inProgressPatterns.add(patCacheKey);
            executor.submit(() -> {
                var slowTextures = createTextures(pattern, patSets, seed, resPerUnit);

                // TextureManager#register doesn't look very thread-safe, so move back to the main thread after the slow part is done
                Minecraft.getInstance().execute(() -> {
                        registerTextures(patCacheKey, slowTextures);
                });
            });
        }
        return Optional.empty();
    }

    private static Map<String, DynamicTexture> createTextures(HexPattern pattern, PatternRenderSettings patSets, double seed, int resPerUnit) {
        HexPatternPoints staticPoints = HexPatternPoints.getStaticPoints(pattern, patSets, seed);

        List<Vec2> zappyRenderSpace = staticPoints.scaleVecs(staticPoints.zappyPoints);

        Map<String, DynamicTexture> patTexts = new HashMap<>();

        NativeImage innerLines = drawLines(zappyRenderSpace, staticPoints, patSets.innerWidthProvider.apply((float)(staticPoints.finalScale)), resPerUnit);
        patTexts.put("inner", new DynamicTexture(innerLines));

        NativeImage outerLines = drawLines(zappyRenderSpace, staticPoints, patSets.outerWidthProvider.apply((float)(staticPoints.finalScale)), resPerUnit);
        patTexts.put("outer", new DynamicTexture(outerLines));

        // TODO: handle start hexagon and grid bits.

//        g2d.setColor(outerColor);
//        g2d.setStroke(new BasicStroke((blockSize * 5f / 3f) * lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
//        drawLines(g2d, points, minX, minY, scale, offsetX, offsetY, padding);
//

//
//        if (showsStrokeOrder) {
//            g2d.setColor(new Color(0xff_d77b5b));
//            Tuple<Integer, Integer> point = getTextureCoordinates(points.get(0), minX, minY, scale, offsetX, offsetY, padding);
//            int spotRadius = circleRadiusByBlockSize * blockSize;
//            drawHexagon(g2d, point.getA(), point.getB(), spotRadius);
//        }


        return patTexts;
    }

    private static Map<String, ResourceLocation> registerTextures(String patTextureKeyBase, Map<String, DynamicTexture> dynamicTextures) {
        // isSlow used to register different textures for the low-resolution, fastly rendered version of each texture
        // and the high-resolution, slowly rendered version (this means the slow doesn't replace the fast in the texture manager,
        // which causes occasional visual stuttering for a frame).
        Map<String, ResourceLocation> resLocs = new HashMap<>();
        for(Map.Entry<String, DynamicTexture> textureEntry : dynamicTextures.entrySet()){
            String name = "hex_pattern_texture_" + patTextureKeyBase + "_" + textureEntry.getKey() + "_" + repaintIndex + ".png";
            ResourceLocation resourceLocation = Minecraft.getInstance().getTextureManager().register(name, textureEntry.getValue());
            resLocs.put(textureEntry.getKey(), resourceLocation);
        }
        patternTexturesToAdd.put(patTextureKeyBase, resLocs);
        return resLocs;
    }

    private static NativeImage drawLines(List<Vec2> points, HexPatternPoints staticPoints, float unscaledLineWidth, int resPerUnit) {
        BufferedImage img = new BufferedImage((int)(staticPoints.fullWidth*resPerUnit), (int)(staticPoints.fullHeight*resPerUnit), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(0xFF_FFFFFF)); // set it to white so we can reuse the texture with different colors
        g2d.setStroke(new BasicStroke(unscaledLineWidth * resPerUnit, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < points.size() - 1; i++) {
            Tuple<Integer, Integer> pointFrom = getTextureCoordinates(points.get(i), staticPoints, resPerUnit);
            Tuple<Integer, Integer> pointTo = getTextureCoordinates(points.get(i+1), staticPoints, resPerUnit);
            g2d.drawLine(pointFrom.getA(), pointFrom.getB(), pointTo.getA(), pointTo.getB());
        }
        g2d.dispose();
        NativeImage nativeImage = new NativeImage(img.getWidth(), img.getHeight(), true);
        for (int y = 0; y < img.getHeight(); y++)
            for (int x = 0; x < img.getWidth(); x++)
                nativeImage.setPixelRGBA(x, y, img.getRGB(x, y));
        return nativeImage;
    }

    private static Tuple<Integer, Integer> getTextureCoordinates(Vec2 point, HexPatternPoints staticPoints, int resPerUnit) {
        int x = (int) ( point.x * resPerUnit);
        int y = (int) ( point.y * resPerUnit);
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