package at.petrak.hexcasting.client.render;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicBookshelf;
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicBookshelf;
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import at.petrak.hexcasting.common.entities.EntityWallScroll;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Helper methods for rendering patterns in the world.
 */
public class WorldlyPatternRenderHelpers {

    public static final PatternRenderSettings WORLDLY_RENDER_SETTINGS = new PatternRenderSettings()
            .withSizings(PatternRenderSettings.FitAxis.BOTH, 1.0, 1.0, 1.0/16, 1.0/16, 0.25, null, null)
            .withWidths((scale) -> 0.4f/16, (scale) -> 0.8f/16)
            .named("worldly");

    public static final PatternRenderSettings SCROLL_RENDER_SETTINGS = WORLDLY_RENDER_SETTINGS.withSizings(null, null, null,
            2.0/16, 2.0/16, null, null, null)
            .named("wallscroll");

    public static final PatternRenderSettings READABLE_SCROLL_RENDER_SETTINGS = SCROLL_RENDER_SETTINGS.withZappySettings(
            null, null, null, null, RenderLib.DEFAULT_READABILITY_OFFSET, RenderLib.DEFAULT_LAST_SEGMENT_LEN_PROP
            )
            .named("wallscroll_readable");

    public static final PatternRenderSettings SLATE_WOMBLY_SETTINGS = WORLDLY_RENDER_SETTINGS.withZappySettings(
            null, 2.5f, 0.1f, null, null, null)
            .named("slate_wobbly");

    // using an opaque inner color based on 0xc8_322b33 because worldly pattern renderer is funky
    public static final PatternColors DEFAULT_PATTERN_COLOR = new PatternColors(0xff_554d54, 0xff_d2c8c8);

    // partially for testing
    public static final PatternColors READABLE_SCROLL_COLORS = DEFAULT_PATTERN_COLOR.withDotColors(0xff_5b7bd7, 0);

    public static final PatternColors SLATE_WOBBLY_COLOR = new PatternColors(RenderLib.screenCol(0xff_64c8ff), 0xff_64c8ff);

    public static void renderPatternForScroll(HexPattern pattern, EntityWallScroll scroll, PoseStack ps, MultiBufferSource bufSource, int light, int blockSize, boolean showStrokeOrder)
    {
        ps.pushPose();
        ps.translate(-blockSize / 2f, -blockSize / 2f, 1f / 32f);
        renderPattern(pattern, showStrokeOrder ? READABLE_SCROLL_RENDER_SETTINGS : SCROLL_RENDER_SETTINGS,
                showStrokeOrder ? READABLE_SCROLL_COLORS : DEFAULT_PATTERN_COLOR,
                scroll.getPos().hashCode(), ps, bufSource, null, null, light, blockSize);
        ps.popPose();
    }

    private static final int[] WALL_ROTATIONS = {180, 270, 0, 90};
    private static final Vec3i[] SLATE_FACINGS = {new Vec3i(0, -1, 0), new Vec3i(-1, -1, 0), new Vec3i(-1, -1, 1), new Vec3i(0, -1 , 1)};
    private static final Vec3[] WALL_NORMALS = {new Vec3(0, 0, -1), new Vec3(-1, 0, 0), new Vec3(0, 0, -1), new Vec3(-1, 0, 0)};
    private static final Vec3i[] SLATE_FLOORCEIL_FACINGS = {new Vec3i(0,0,0), new Vec3i(1,0,0), new Vec3i(1,0,1), new Vec3i(0,0,1)};

    public static void renderPatternForSlate(BlockEntitySlate tile, HexPattern pattern, PoseStack ps, MultiBufferSource buffer, int light, BlockState bs)
    {

        boolean isOnWall = bs.getValue(BlockSlate.ATTACH_FACE) == AttachFace.WALL;
        boolean isOnCeiling = bs.getValue(BlockSlate.ATTACH_FACE) == AttachFace.CEILING;
        int facing = bs.getValue(BlockSlate.FACING).get2DDataValue();

        boolean wombly = bs.getValue(BlockSlate.ENERGIZED);

        ps.pushPose();

        Vec3 normal = null;
        if(isOnWall){
            ps.mulPose(Axis.ZP.rotationDegrees(180));
            Vec3i tV = SLATE_FACINGS[facing % 4];
            ps.translate(tV.getX(), tV.getY(), tV.getZ());
            ps.mulPose(Axis.YP.rotationDegrees(WALL_ROTATIONS[facing % 4]));
            normal = WALL_NORMALS[facing % 4];
        } else {
            Vec3i tV = SLATE_FLOORCEIL_FACINGS[facing % 4];
            ps.translate(tV.getX(), tV.getY(), tV.getZ());

            ps.mulPose(Axis.YP.rotationDegrees(facing*-90));
            ps.mulPose(Axis.XP.rotationDegrees(90 * (isOnCeiling ? -1 : 1)));
            if(isOnCeiling) ps.translate(0,-1,1);

            normal = new Vec3(0, 0, -1);
        }

        renderPattern(pattern,
                wombly ? SLATE_WOMBLY_SETTINGS : WORLDLY_RENDER_SETTINGS,
                wombly ? SLATE_WOBBLY_COLOR : DEFAULT_PATTERN_COLOR,
                tile.getBlockPos().hashCode(), ps, buffer, normal, null, light, 1);
        ps.popPose();
    }

    private static final Vec3i[] BLOCK_FACINGS = {new Vec3i(0, -1, 1), new Vec3i(0, -1, 0), new Vec3i(-1, -1, 0), new Vec3i(-1, -1, 1)};

    public static void renderPatternForAkashicBookshelf(BlockEntityAkashicBookshelf tile, HexPattern pattern, PoseStack ps, MultiBufferSource buffer, int light, BlockState bs)
    {

        int facing = bs.getValue(BlockAkashicBookshelf.FACING).get2DDataValue();

        ps.pushPose();
        ps.mulPose(Axis.ZP.rotationDegrees(180));

        Vec3i tV = BLOCK_FACINGS[facing % 4];
        ps.translate(tV.getX(), tV.getY(), tV.getZ());
        ps.mulPose(Axis.YP.rotationDegrees(WALL_ROTATIONS[facing % 4]));

        renderPattern(pattern, WORLDLY_RENDER_SETTINGS, DEFAULT_PATTERN_COLOR,
                tile.getBlockPos().hashCode(), ps, buffer, WALL_NORMALS[facing % 4], -0.02f, light, 1);
        ps.popPose();
    }

    /**
     * Renders a pattern in world space based on the given transform requirements
     */
    public static void renderPattern(HexPattern pattern, PatternRenderSettings patSets, PatternColors patColors,
        double seed, PoseStack ps, MultiBufferSource bufSource, Vec3 normal, @Nullable Float zOffset,
        int light, int blockSize)
    {

        ps.pushPose();


        float z = zOffset != null ? zOffset : ((-1f / 16f) - 0.01f);

        normal = normal != null ? normal : new Vec3(0, 0, -1);

        ps.scale(blockSize, blockSize, 1);
        ps.translate(0,0, z);

        PatternRenderer.renderPattern(pattern, ps, new PatternRenderer.WorldlyBits(bufSource, light, normal),
                patSets, patColors, seed, blockSize * PatternTextureManager.resolutionByBlockSize);

        ps.popPose();
    }
}
