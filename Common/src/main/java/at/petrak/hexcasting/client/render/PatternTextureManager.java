package at.petrak.hexcasting.client.render;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicBookshelf;
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicBookshelf;
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec2;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class PatternTextureManager {

    //TODO: remove if not needed anymore for comparison
    public static boolean useTextures = true;
    public static int repaintIndex = 0;

    public static int resolutionByBlockSize = 512;
    public static int paddingByBlockSize = 50;
    public static int circleRadiusByBlockSize = 8;
    public static int scaleLimit = 16;

    private static HashMap<String, ResourceLocation> patternTextures = new HashMap<>();

    public static String getPointsKey(List<Vec2> zappyPoints)
    {
        return zappyPoints.stream()
                .map(p -> String.format("(%f,%f)", p.x, p.y))
                .collect(Collectors.joining(";"));
    }

    public static HexPatternPoints generateHexPatternPoints(HexBlockEntity tile, HexPattern pattern, float flowIrregular)
    {
        var stupidHash = tile.getBlockPos().hashCode();
        var lines1 = pattern.toLines(1, Vec2.ZERO);
        var zappyPoints = RenderLib.makeZappy(lines1, RenderLib.findDupIndices(pattern.positions()),
                10, 0.5f, 0f, flowIrregular, 0f, 1f, stupidHash);
        return new HexPatternPoints(zappyPoints);
    }

    public static void renderPatternForScroll(String pointsKey, PoseStack ps, MultiBufferSource bufSource, int light, List<Vec2> zappyPoints, int blockSize, boolean showStrokeOrder)
    {
        renderPattern(pointsKey, ps, bufSource, light, zappyPoints, blockSize, showStrokeOrder, false, true, false,false, true,-1);
    }
    public static void renderPatternForSlate(BlockEntitySlate tile, HexPattern pattern, PoseStack ps, MultiBufferSource buffer, int light, BlockState bs)
    {
        if(tile.points == null)
            tile.points = generateHexPatternPoints(tile, pattern, 0.2f);

        boolean isOnWall = bs.getValue(BlockSlate.ATTACH_FACE) == AttachFace.WALL;
        boolean isOnCeiling = bs.getValue(BlockSlate.ATTACH_FACE) == AttachFace.CEILING;
        int facing = bs.getValue(BlockSlate.FACING).get2DDataValue();

        renderPatternForBlockEntity(tile.points, ps, buffer, light, isOnWall, isOnCeiling, true, facing);
    }
    public static void renderPatternForAkashicBookshelf(BlockEntityAkashicBookshelf tile, HexPattern pattern, PoseStack ps, MultiBufferSource buffer, int light, BlockState bs)
    {
        if(tile.points == null)
            tile.points = generateHexPatternPoints(tile, pattern, 0f);

        int facing = bs.getValue(BlockAkashicBookshelf.FACING).get2DDataValue();
        renderPatternForBlockEntity(tile.points, ps, buffer, light, true, false, false, facing);
    }

    public static void renderPatternForBlockEntity(HexPatternPoints points, PoseStack ps, MultiBufferSource buffer, int light, boolean isOnWall, boolean isOnCeiling, boolean isSlate, int facing)
    {
        var oldShader = RenderSystem.getShader();
        ps.pushPose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        renderPattern(points.pointsKey, ps, buffer, light, points.zappyPoints, 1, false, true, isOnWall, isOnCeiling, isSlate, false, facing);
        ps.popPose();
        RenderSystem.setShader(() -> oldShader);
    }

    public static void renderPattern(String pointsKey, PoseStack ps, MultiBufferSource bufSource, int light, List<Vec2> zappyPoints, int blockSize, boolean showStrokeOrder, boolean useFullSize, boolean isOnWall, boolean isOnCeiling, boolean isSlate, boolean isScroll, int facing)
    {
        ps.pushPose();

        PoseStack.Pose last = ps.last();
        Matrix4f mat = last.pose();
        Matrix3f normal = last.normal();

        float x = blockSize, y = blockSize, z = (-1f / 16f) - 0.01f;
        float nx = 0, ny = 0, nz = 0;

        //TODO: refactor this mess of a method

        if(isOnWall)
        {
            if(isScroll)
            {
                ps.translate(-blockSize / 2f, -blockSize / 2f, 1f / 32f);
                nz = -1;
            }
            else
            {
                ps.mulPose(Vector3f.ZP.rotationDegrees(180));

                if(isSlate)
                {
                    if(facing == 0)
                        ps.translate(0,-1,0);
                    if(facing == 1)
                        ps.translate(-1,-1,0);
                    if(facing == 2)
                        ps.translate(-1,-1,1);
                    if(facing == 3)
                        ps.translate(0,-1,1);
                }
                else
                {
                    z = -0.01f;
                    if(facing == 0)
                        ps.translate(0,-1,1);
                    if(facing == 1)
                        ps.translate(0,-1,0);
                    if(facing == 2)
                        ps.translate(-1,-1,0);
                    if(facing == 3)
                        ps.translate(-1,-1,1);
                }

                if(facing == 0)
                    ps.mulPose(Vector3f.YP.rotationDegrees(180));
                if(facing == 1)
                    ps.mulPose(Vector3f.YP.rotationDegrees(270));
                if(facing == 3)
                    ps.mulPose(Vector3f.YP.rotationDegrees(90));

                if(facing == 0 || facing == 2)
                    nz = -1;
                if(facing == 1 || facing == 3)
                    nx = -1;
                ps.translate(0,0,0);
            }
        }
        else //slates on the floor or ceiling
        {
            if(facing == 0)
                ps.translate(0,0,0);
            if(facing == 1)
                ps.translate(1,0,0);
            if(facing == 2)
                ps.translate(1,0,1);
            if(facing == 3)
                ps.translate(0,0,1);
            ps.mulPose(Vector3f.YP.rotationDegrees(facing*-90));

            if(isOnCeiling)
            {
                ps.mulPose(Vector3f.XP.rotationDegrees(-90));
                ps.translate(0,-1,1);
            }
            else
                ps.mulPose(Vector3f.XP.rotationDegrees(90));
            nz = -1;
        }

        int lineWidth = 16;
        int outerColor = 0xB4B4BE;//0xff_c8c8d2;
        int innerColor = 0x2A2A2A;//0xc8_322b33;
        if(isScroll)
        {
            lineWidth = 20;
            outerColor = 0xDEDEDE;//0xff_d2c8c8;
            innerColor = 0x343434;//0xc8_322b33;
        }

        ResourceLocation texture = getTexture(zappyPoints, pointsKey, blockSize, showStrokeOrder, lineWidth, useFullSize, new Color(innerColor), new Color(outerColor));
        VertexConsumer verts = bufSource.getBuffer(RenderType.entityCutout(texture));

        vertex(mat, normal, light, verts, 0, 0, z, 0, 0, nx, ny, nz);
        vertex(mat, normal, light, verts, 0, y, z, 0, 1, nx, ny, nz);
        vertex(mat, normal, light, verts, x, y, z, 1, 1, nx, ny, nz);
        vertex(mat, normal, light, verts, x, 0, z, 1, 0, nx, ny, nz);

        ps.popPose();
    }

    private static void vertex(Matrix4f mat, Matrix3f normal, int light, VertexConsumer verts, float x, float y, float z,
                               float u, float v, float nx, float ny, float nz) {
        verts.vertex(mat, x, y, z)
                .color(0xffffffff)
                .uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, nx, ny, nz)
                .endVertex();
    }

    public static ResourceLocation getTexture(List<Vec2> points, String pointsKey, int blockSize, boolean showsStrokeOrder, int lineWidth, boolean useFullSize, Color innerColor, Color outerColor) {
        if (patternTextures.containsKey(pointsKey))
            return patternTextures.get(pointsKey);
        return createTexture(points, pointsKey, blockSize, showsStrokeOrder, lineWidth, useFullSize, innerColor, outerColor);
    }

    public static ResourceLocation createTexture(List<Vec2> points, String pointsKey, int blockSize, boolean showsStrokeOrder, int lineWidth, boolean useFullSize, Color innerColor, Color outerColor)
    {
        int resolution = resolutionByBlockSize * blockSize;
        int padding = paddingByBlockSize * blockSize;

        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE, minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
        for (Vec2 point : points)
        {
            minX = Math.min(minX, point.x);
            maxX = Math.max(maxX, point.x);
            minY = Math.min(minY, point.y);
            maxY = Math.max(maxY, point.y);
        }

        double rangeX = maxX - minX;
        double rangeY = maxY - minY;

        double scale = Math.min((resolution - 2 * padding) / rangeX, (resolution - 2 * padding) / rangeY);

        double limit = blockSize * scaleLimit;
        if (!useFullSize && scale > limit)
            scale = limit;

        double offsetX = ((resolution - 2 * padding) - rangeX * scale) / 2;
        double offsetY = ((resolution - 2 * padding) - rangeY * scale) / 2;

        BufferedImage img = new BufferedImage(resolution, resolution, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        g2d.setColor(outerColor);
        g2d.setStroke(new BasicStroke((blockSize * 5f / 3f) * lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        drawLines(g2d, points, minX, minY, scale, offsetX, offsetY, padding);

        g2d.setColor(innerColor);
        g2d.setStroke(new BasicStroke((blockSize * 2f / 3f) * lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        drawLines(g2d, points, minX, minY, scale, offsetX, offsetY, padding);


        if (showsStrokeOrder) {
            g2d.setColor(new Color(0xff_d77b5b));
            Tuple<Integer, Integer> point = getTextureCoordinates(points.get(0), minX, minY, scale, offsetX, offsetY, padding);
            int spotRadius = circleRadiusByBlockSize * blockSize;
            drawHexagon(g2d, point.getA(), point.getB(), spotRadius);
        }

        g2d.dispose();

        NativeImage nativeImage = new NativeImage(img.getWidth(), img.getHeight(), true);
        for (int y = 0; y < img.getHeight(); y++)
            for (int x = 0; x < img.getWidth(); x++)
                nativeImage.setPixelRGBA(x, y, img.getRGB(x, y));

        DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);
        ResourceLocation resourceLocation = Minecraft.getInstance().getTextureManager().register("hex_pattern_texture_" + points.hashCode() + "_" + repaintIndex + ".png", dynamicTexture);
        patternTextures.put(pointsKey, resourceLocation);
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
        return new Tuple(x, y);
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
        patternTextures.clear();
    }
}
