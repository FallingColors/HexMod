package at.petrak.hexcasting.client.render;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicBookshelf;
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicBookshelf;
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.List;

/**
 * Helper methods for rendering patterns in the world.
 */
public class WorldlyPatternRenderHelpers {
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

    /**
     * Renders a pattern in world space based on the given transform requirements
     */
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
                ps.mulPose(Axis.ZP.rotationDegrees(180));

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
                    ps.mulPose(Axis.YP.rotationDegrees(180));
                if(facing == 1)
                    ps.mulPose(Axis.YP.rotationDegrees(270));
                if(facing == 3)
                    ps.mulPose(Axis.YP.rotationDegrees(90));

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
            ps.mulPose(Axis.YP.rotationDegrees(facing*-90));

            if(isOnCeiling)
            {
                ps.mulPose(Axis.XP.rotationDegrees(-90));
                ps.translate(0,-1,1);
            }
            else
                ps.mulPose(Axis.XP.rotationDegrees(90));
            nz = -1;
        }

        int lineWidth = PatternTextureManager.otherLineWidth;
        int outerColor = 0xff_d2c8c8;
        int innerColor = 0xc8_322b33;
        if(isScroll)
            lineWidth = PatternTextureManager.scrollLineWidth;

        ResourceLocation texture = PatternTextureManager.getTexture(zappyPoints, pointsKey, blockSize, showStrokeOrder, lineWidth, useFullSize, new Color(innerColor), new Color(outerColor));
        VertexConsumer verts = bufSource.getBuffer(RenderType.entityCutout(texture));

        vertex(mat, normal, light, verts, 0, 0, z, 0, 0, nx, ny, nz);
        vertex(mat, normal, light, verts, 0, y, z, 0, 1, nx, ny, nz);
        vertex(mat, normal, light, verts, x, y, z, 1, 1, nx, ny, nz);
        vertex(mat, normal, light, verts, x, 0, z, 1, 0, nx, ny, nz);

        ps.popPose();
    }



    public static HexPatternPoints generateHexPatternPoints(HexBlockEntity tile, HexPattern pattern, float flowIrregular)
    {
        var stupidHash = tile.getBlockPos().hashCode();
        var lines1 = pattern.toLines(1, Vec2.ZERO);
        var zappyPoints = RenderLib.makeZappy(lines1, RenderLib.findDupIndices(pattern.positions()),
                10, 0.5f, 0f, flowIrregular, 0f, 1f, stupidHash);
        return new HexPatternPoints(zappyPoints);
    }

    private static void vertex(Matrix4f mat, Matrix3f normal, int light, VertexConsumer verts, float x, float y, float z,
                                 float u, float v, float nx, float ny, float nz) {
        verts.vertex(mat, x, y, z)
                .color(0xffffffff)
                .uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, nx, ny, nz)
                .endVertex();
    }
}
