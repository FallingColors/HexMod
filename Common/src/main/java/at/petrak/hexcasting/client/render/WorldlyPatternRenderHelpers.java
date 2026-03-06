package at.petrak.hexcasting.client.render;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicBookshelf;
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicBookshelf;
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import at.petrak.hexcasting.common.entities.EntityWallScroll;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;

import javax.annotation.Nullable;

/**
 * Helper methods for rendering patterns in the world.
 */
public class WorldlyPatternRenderHelpers {

    public static final PatternSettings SCROLL_SETTINGS = new PatternSettings("scroll",
            PatternSettings.PositionSettings.paddedSquare(2.0/16),
            PatternSettings.StrokeSettings.fromStroke(0.8/16),
            PatternSettings.ZappySettings.STATIC
    );

    public static final PatternSettings READABLE_SCROLL_SETTINGS = new PatternSettings("scroll_readable",
            PatternSettings.PositionSettings.paddedSquare(2.0/16),
            PatternSettings.StrokeSettings.fromStroke(0.8/16),
            PatternSettings.ZappySettings.READABLE
    );

    public static final PatternSettings WORLDLY_SETTINGS = new PatternSettings("worldly",
            PatternSettings.PositionSettings.paddedSquare(2.0/16),
            PatternSettings.StrokeSettings.fromStroke(0.8/16),
            PatternSettings.ZappySettings.STATIC
    );

    public static final PatternSettings WORLDLY_SETTINGS_WOBBLY = new PatternSettings("wobbly_world",
            PatternSettings.PositionSettings.paddedSquare(2.0/16),
            PatternSettings.StrokeSettings.fromStroke(0.8/16),
            PatternSettings.ZappySettings.WOBBLY
    );

    public static void renderPatternForScroll(HexPattern pattern, EntityWallScroll scroll, PoseStack ps, MultiBufferSource bufSource, int light, int blockSize, boolean showStrokeOrder)
    {
        ps.pushPose();
        ps.translate(-blockSize / 2f, -blockSize / 2f, 1f / 32f);
        // there's almost certainly a better way to do this, but we're just flipping the y and z axes to fix normals
        ps.last().normal().mul(new Matrix3f(1, 0, 0, 0, 0, 1, 0, 1, 0));
        renderPattern(pattern, showStrokeOrder ? READABLE_SCROLL_SETTINGS : SCROLL_SETTINGS,
                showStrokeOrder ? PatternColors.READABLE_SCROLL_COLORS : PatternColors.DEFAULT_PATTERN_COLOR,
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
        }

        renderPattern(pattern,
                wombly ? WORLDLY_SETTINGS_WOBBLY : WORLDLY_SETTINGS,
                wombly ? PatternColors.SLATE_WOBBLY_PURPLE_COLOR : PatternColors.DEFAULT_PATTERN_COLOR,
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

        int actualLight = LevelRenderer.getLightColor(tile.getLevel(), tile.getBlockPos().relative(bs.getValue(BlockAkashicBookshelf.FACING)));

        renderPattern(pattern, WORLDLY_SETTINGS , PatternColors.DEFAULT_PATTERN_COLOR,
                tile.getBlockPos().hashCode(), ps, buffer, WALL_NORMALS[facing % 4].multiply(-1, -1, -1), -0.02f, actualLight, 1);
        ps.popPose();
    }

    /**
     * Renders a pattern in world space based on the given transform requirements
     */
    public static void renderPattern(HexPattern pattern, PatternSettings patSets, PatternColors patColors,
        double seed, PoseStack ps, MultiBufferSource bufSource, Vec3 normal, @Nullable Float zOffset,
        int light, int blockSize)
    {

        ps.pushPose();


        float z = zOffset != null ? zOffset : ((-1f / 16f) - 0.01f);

        normal = normal != null ? normal : new Vec3(0, 0, -1);

        ps.translate(0,0, z);
        ps.scale(blockSize, blockSize, 1);


        PoseStack noNormalInv = new PoseStack();
        noNormalInv.scale(1, 1, -1);
        ps.mulPose(noNormalInv.last().pose());

        PatternRenderer.renderPattern(pattern, ps, new PatternRenderer.WorldlyBits(bufSource, light, normal),
                patSets, patColors, seed, blockSize * 512);

        ps.popPose();
    }
}
