package at.petrak.hexcasting.client.render;

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

    private static final ConcurrentMap<String, Map<String, ResourceLocation>> patternTexturesToAdd = new ConcurrentHashMap<>();
    private static final Set<String> inProgressPatterns = new HashSet<>();
    // basically newCachedThreadPool, but with a max pool size
    private static final ExecutorService executor = new ThreadPoolExecutor(0, 16, 60L, TimeUnit.SECONDS, new LinkedBlockingDeque<>());

    private static final HashMap<String, Map<String, ResourceLocation>> patternTextures = new HashMap<>();

    public static Optional<Map<String, ResourceLocation>> getTextures(HexPatternLike patternlike, PatternSettings patSets, double seed, int resPerUnit) {
        String patCacheKey = patSets.getCacheKey(patternlike, seed) + "_" + resPerUnit;

        // move textures from concurrent map to normal hashmap as needed
        if (patternTexturesToAdd.containsKey(patCacheKey)) {
            var patternTexture = patternTexturesToAdd.remove(patCacheKey);
            var oldPatternTexture = patternTextures.put(patCacheKey, patternTexture);
            inProgressPatterns.remove(patCacheKey);
            if (oldPatternTexture != null) // TODO: is this needed? when does this ever happen?
                for(ResourceLocation oldPatternTextureSingle : oldPatternTexture.values())
                    Minecraft.getInstance().getTextureManager().getTexture(oldPatternTextureSingle).close();

            return Optional.empty(); // try not giving it immediately to avoid flickering?
        }
        if (patternTextures.containsKey(patCacheKey))
            return Optional.of(patternTextures.get(patCacheKey));

        // render a higher-resolution texture in a background thread so it eventually becomes all nice nice and pretty
        if(!inProgressPatterns.contains(patCacheKey)){
            inProgressPatterns.add(patCacheKey);
            executor.submit(() -> {
                var slowTextures = createTextures(patternlike, patSets, seed, resPerUnit);

                // TextureManager#register doesn't look very thread-safe, so move back to the main thread after the slow part is done
                Minecraft.getInstance().execute(() -> {
                        registerTextures(patCacheKey, slowTextures);
                });
            });
        }
        return Optional.empty();
    }

    private static Map<String, DynamicTexture> createTextures(HexPatternLike patternlike, PatternSettings patSets, double seed, int resPerUnit) {
        HexPatternPoints staticPoints = HexPatternPoints.getStaticPoints(patternlike, patSets, seed);

        List<Vec2> zappyRenderSpace = staticPoints.scaleVecs(staticPoints.zappyPoints);

        Map<String, DynamicTexture> patTexts = new HashMap<>();

        NativeImage innerLines = drawLines(zappyRenderSpace, staticPoints, (float)patSets.getInnerWidth((staticPoints.finalScale)), resPerUnit);
        patTexts.put("inner", new DynamicTexture(innerLines));

        NativeImage outerLines = drawLines(zappyRenderSpace, staticPoints, (float)patSets.getOuterWidth((staticPoints.finalScale)), resPerUnit);
        patTexts.put("outer", new DynamicTexture(outerLines));

        return patTexts;
    }

    private static Map<String, ResourceLocation> registerTextures(String patTextureKeyBase, Map<String, DynamicTexture> dynamicTextures) {
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

    // keeping this around just in case we ever decide to put the dots in the textures instead of dynamic
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