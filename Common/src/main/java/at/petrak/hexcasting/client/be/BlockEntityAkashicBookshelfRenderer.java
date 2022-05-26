package at.petrak.hexcasting.client.be;

import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.client.RenderLib;
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicBookshelf;
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicBookshelf;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public class BlockEntityAkashicBookshelfRenderer implements BlockEntityRenderer<BlockEntityAkashicBookshelf> {
    public BlockEntityAkashicBookshelfRenderer(BlockEntityRendererProvider.Context ctx) {
        // NO-OP
    }

    @Override
    public void render(BlockEntityAkashicBookshelf tile, float pPartialTick, PoseStack ps,
        MultiBufferSource buffer, int light, int overlay) {
        HexPattern pattern = tile.getPattern();
        if (pattern == null) {
            return;
        }

        var bs = tile.getBlockState();

        var oldShader = RenderSystem.getShader();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableDepthTest();

        ps.pushPose();

        ps.translate(0.5, 0.5, 0.5);
        var quarters = (-bs.getValue(BlockAkashicBookshelf.FACING).get2DDataValue()) % 4;
        ps.mulPose(new Quaternion(Vector3f.YP, Mth.HALF_PI * quarters, false));
        ps.mulPose(new Quaternion(Vector3f.ZP, Mth.PI, false));

        // and now Z is out?
        ps.translate(0, 0, 0.5);
        ps.scale(1 / 16f, 1 / 16f, 1 / 16f);
        ps.translate(0, 0, 0.01);

        // yoink code from the pattern greeble
        // Do two passes: one with a random size to find a good COM and one with the real calculation
        var com1 = pattern.getCenter(1);
        var lines1 = pattern.toLines(1, Vec2.ZERO);

        var maxDx = -1f;
        var maxDy = -1f;
        for (var dot : lines1) {
            var dx = Mth.abs(dot.x - com1.x);
            if (dx > maxDx) {
                maxDx = dx;
            }
            var dy = Mth.abs(dot.y - com1.y);
            if (dy > maxDy) {
                maxDy = dy;
            }
        }
        var scale = Math.min(3.8f, Math.min(16 / 2.5f / maxDx, 16 / 2.5f / maxDy));

        var com2 = pattern.getCenter(scale);
        var lines2 = pattern.toLines(scale, com2.negated());
        // For some reason it is mirrored left to right and i can't seem to posestack-fu it into shape
        for (int j = 0; j < lines2.size(); j++) {
            var v = lines2.get(j);
            lines2.set(j, new Vec2(-v.x, v.y));
        }

        var zappy = RenderLib.makeZappy(lines2, 10f, 0.5f, 0f, 0f);

        int outer = 0xff_d2c8c8;
        int inner = 0xc8_322b33;
        RenderLib.drawLineSeq(ps.last().pose(), zappy, 1f, 0f, outer, outer);
        RenderLib.drawLineSeq(ps.last().pose(), zappy, 0.4f, 0.01f, inner, inner);

        ps.popPose();
        RenderSystem.setShader(() -> oldShader);
    }
}
