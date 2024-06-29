package at.petrak.hexcasting.client.render.be;

import at.petrak.hexcasting.client.render.PatternTextureManager;
import at.petrak.hexcasting.client.render.RenderLib;
import at.petrak.hexcasting.client.render.WorldlyPatternRenderHelpers;
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;

public class BlockEntitySlateRenderer implements BlockEntityRenderer<BlockEntitySlate> {
    public BlockEntitySlateRenderer(BlockEntityRendererProvider.Context ctx) {
        // NO-OP
    }

    @Override
    public void render(BlockEntitySlate tile, float pPartialTick, PoseStack ps,
        MultiBufferSource buffer, int light, int overlay) {
        if (tile.pattern == null)
            return;

        var bs = tile.getBlockState();

        if(PatternTextureManager.useTextures && !bs.getValue(BlockSlate.ENERGIZED)) {
            WorldlyPatternRenderHelpers.renderPatternForSlate(tile, tile.pattern, ps, buffer, light, bs);
            return;
        }

        //TODO: remove old rendering if not needed anymore for comparison

        var oldShader = RenderSystem.getShader();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableDepthTest();

        ps.pushPose();

        ps.translate(0.5, 0.5, 0.5);
        var attchFace = bs.getValue(BlockSlate.ATTACH_FACE);
        if (attchFace == AttachFace.WALL) {
            var quarters = (-bs.getValue(BlockSlate.FACING).get2DDataValue()) % 4;
            ps.mulPose(Axis.YP.rotation(Mth.HALF_PI * quarters));
            ps.mulPose(Axis.ZP.rotation(Mth.PI));
        } else {
            var neg = attchFace == AttachFace.FLOOR ? -1 : 1;
            ps.mulPose(Axis.XP.rotation(neg * Mth.HALF_PI));
            var quarters = (bs.getValue(BlockSlate.FACING).get2DDataValue() + 2) % 4;
            ps.mulPose(Axis.ZP.rotation(neg * Mth.HALF_PI * quarters));
        }

        // Resolution is the number of sub-voxels in the block for rendering purposes, 16 is the default
        // padding is the space to leave on the edges free of pattern
        var resolution = 16;
        var padding = resolution * PatternTextureManager.paddingByBlockSize / PatternTextureManager.resolutionByBlockSize;

        // and now Z is out?
        ps.translate(0, 0, -0.5);
        ps.scale(1f / resolution, 1f / resolution, 1f / resolution);
        ps.translate(0, 0, 1.01);

        var isLit = bs.getValue(BlockSlate.ENERGIZED);
        var variance = isLit ? 2.5f : 0.5f;
        var speed = isLit ? 0.1f : 0f;

        var lines1 = tile.pattern.toLines(1, Vec2.ZERO);
        var stupidHash = tile.getBlockPos().hashCode();
        var zappyPattern = RenderLib.makeZappy(lines1, RenderLib.findDupIndices(tile.pattern.positions()),
                10, variance, speed, 0.2f, 0f, 1f, stupidHash);

        // always do space calculations with the static version of the pattern
        // so that it doesn't jump around resizing itself.
        var zappyPatternSpace = RenderLib.makeZappy(lines1, RenderLib.findDupIndices(tile.pattern.positions()),
                10, 0.5f, 0f, 0.2f, 0f, 1f, stupidHash);

        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE, minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
        for (Vec2 point : zappyPatternSpace)
        {
            minX = Math.min(minX, point.x);
            maxX = Math.max(maxX, point.x);
            minY = Math.min(minY, point.y);
            maxY = Math.max(maxY, point.y);
        }

        double rangeX = maxX - minX;
        double rangeY = maxY - minY;

        double scale = Math.min((resolution - 2 * padding) / rangeX, (resolution - 2 * padding) / rangeY);

        double offsetX = ((- 2 * padding) - rangeX * scale) / 2;
        double offsetY = ((- 2 * padding) - rangeY * scale) / 2;

        var zappyRenderSpace = new ArrayList<Vec2>();

        for (Vec2 point : zappyPattern) {
            zappyRenderSpace.add(new Vec2(
                (float) (((point.x - minX) * scale + offsetX) + padding),
                (float) (((point.y - minY) * scale + offsetY) + padding)
            ));
        }

        // For some reason it is mirrored left to right and i can't seem to posestack-fu it into shape
        for (int i = 0; i < zappyRenderSpace.size(); i++) {
            var v = zappyRenderSpace.get(i);
            zappyRenderSpace.set(i, new Vec2(-v.x, v.y));
        }

        int outer = isLit ? 0xff_64c8ff : 0xff_d2c8c8;
        int inner = isLit ? RenderLib.screenCol(outer) : 0xc8_322b33;
        RenderLib.drawLineSeq(ps.last().pose(), zappyRenderSpace, 1f, 0f, outer, outer);
        RenderLib.drawLineSeq(ps.last().pose(), zappyRenderSpace, 0.4f, 0.01f, inner, inner);

        ps.popPose();
        RenderSystem.setShader(() -> oldShader);
    }
}