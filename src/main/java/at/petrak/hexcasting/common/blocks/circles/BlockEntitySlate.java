package at.petrak.hexcasting.common.blocks.circles;

import at.petrak.hexcasting.client.RenderLib;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import at.petrak.hexcasting.common.blocks.ModBlockEntity;
import at.petrak.hexcasting.hexmath.HexPattern;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;

public class BlockEntitySlate extends ModBlockEntity {
    public static final String TAG_PATTERN = "pattern";

    @Nullable
    public HexPattern pattern;

    public BlockEntitySlate(BlockPos pos, BlockState state) {
        super(HexBlocks.SLATE_TILE.get(), pos, state);
    }

    @Override
    protected void saveModData(CompoundTag tag) {
        if (this.pattern != null) {
            tag.put(TAG_PATTERN, this.pattern.serializeToNBT());
        }
    }

    @Override
    protected void loadModData(CompoundTag tag) {
        if (tag.contains(TAG_PATTERN)) {
            this.pattern = HexPattern.DeserializeFromNBT(tag.getCompound(TAG_PATTERN));
        }
    }

    public static class Renderer implements BlockEntityRenderer<BlockEntitySlate> {
        private final BlockRenderDispatcher dispatcher;

        public Renderer(BlockEntityRendererProvider.Context ctx) {
            this.dispatcher = ctx.getBlockRenderDispatcher();
        }

        @Override
        public void render(BlockEntitySlate tile, float pPartialTick, PoseStack ps,
            MultiBufferSource buffer, int light, int overlay) {
            if (tile.pattern == null) {
                return;
            }

            var bs = tile.getBlockState();

            var oldShader = RenderSystem.getShader();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.enableDepthTest();

            ps.pushPose();

            // This puts the origin at the -X, +Y, -Z corner of the block, for some reason
            ps.mulPose(new Quaternion(Vector3f.XN, Mth.HALF_PI, false));
            // and now Z is up?
            ps.translate(0.5, -0.5, 0);
            // Now rotate it so we face the right direction
            var quarters = (-bs.getValue(BlockSlate.FACING).get2DDataValue() + 2) % 4;
            ps.mulPose(new Quaternion(Vector3f.ZP, Mth.HALF_PI * quarters, false));
            ps.scale(1 / 16f, 1 / 16f, 1 / 16f);
            ps.translate(0, 0, 1.1);

            // yoink code from the pattern greeble
            // Do two passes: one with a random size to find a good COM and one with the real calculation
            var com1 = tile.pattern.getCenter(1);
            var lines1 = tile.pattern.toLines(1, Vec2.ZERO);

            var maxDist = -1f;
            for (var dot : lines1) {
                var dist = Mth.sqrt(dot.distanceToSqr(com1));
                if (dist > maxDist) {
                    maxDist = dist;
                }
            }
            var scale = Math.min(4, 16 / 2.5f / maxDist);

            var com2 = tile.pattern.getCenter(scale);
            var lines2 = tile.pattern.toLines(scale, com2.negated());
            // For some reason it is mirrored left to right and i can't seem to posestack-fu it into shape
            for (int i = 0; i < lines2.size(); i++) {
                var v = lines2.get(i);
                lines2.set(i, new Vec2(-v.x, v.y));
            }

            var isLit = bs.getValue(BlockSlate.ENERGIZED);
            var zappy = RenderLib.makeZappy(lines2, 10f, isLit ? 2.5f : 0.5f, isLit ? 0.1f : 0f);

            int outer = isLit ? 0xff_64c8ff : 0xff_d2c8c8;
            int inner = isLit ? RenderLib.screenCol(outer) : 0xc8_322b33;
            RenderLib.drawLineSeq(ps.last().pose(), zappy, 1f, 0f, outer, outer);
            RenderLib.drawLineSeq(ps.last().pose(), zappy, 0.4f, 0.1f, inner, inner);

            ps.popPose();
            RenderSystem.setShader(() -> oldShader);
        }
    }
}
