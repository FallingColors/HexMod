package at.petrak.hexcasting.api.casting.circles;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.lib.HexSounds;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * Implement this on a block to make circles interact with it.
 * <p>
 * This is its own interface so you can have your blocks subclass something else, and to avoid enormous
 * files. The mod doesn't check for the interface on anything but blocks.
 */
public interface ICircleComponent {
    /**
     * The heart of the interface! Functionally modify the casting environment.
     * <p>
     * With the new update you can have the side effects happen inline. In fact, you have to have the side effects
     * happen inline.
     * <p>
     * Also, return a list of directions that the control flow can exit this block in.
     * The circle environment will mishap if not exactly 1 of the returned directions can be accepted from.
     */
    ControlFlow acceptControlFlow(CastingImage imageIn, CircleCastEnv env, Direction enterDir, BlockPos pos,
        BlockState bs, ServerLevel world);

    /**
     * Can this component get transferred to from a block coming in from that direction, with the given normal?
     */
    @Contract(pure = true)
    boolean canEnterFromDirection(Direction enterDir, BlockPos pos, BlockState bs, ServerLevel world);

    /**
     * This determines the directions the control flow <em>can</em> exit from. It's called at the beginning of execution
     * to see if the circle actually forms a loop.
     * <p>
     * For most blocks, this should be the same as returned from {@link ICircleComponent#acceptControlFlow}
     * Things like directrices might return otherwise. Whatever is returned when controlling flow must be a subset of
     * this set.
     */
    @Contract(pure = true)
    EnumSet<Direction> possibleExitDirections(BlockPos pos, BlockState bs, Level world);

    /**
     * Given the current position and a direction, return a pair of the new position after a step
     * in that direction, along with the direction (this is a helper function for creating
     * {@link ICircleComponent.ControlFlow}s.
     */
    @Contract(pure = true)
    default Pair<BlockPos, Direction> exitPositionFromDirection(BlockPos pos, Direction dir) {
        return Pair.of(pos.offset(dir.getStepX(), dir.getStepY(), dir.getStepZ()), dir);
    }

    /**
     * Start the {@link ICircleComponent} at the given position glowing. Returns the new state
     * of the given block.
     * // TODO: determine if this should just be in
     * {@link ICircleComponent#acceptControlFlow(CastingImage, CircleCastEnv, Direction, BlockPos, BlockState, ServerLevel)}.
     */
    BlockState startEnergized(BlockPos pos, BlockState bs, Level world);

    /**
     * Returns whether the {@link ICircleComponent} at the given position is energized.
     */
    boolean isEnergized(BlockPos pos, BlockState bs, Level world);

    /**
     * End the {@link ICircleComponent} at the given position glowing. Returns the new state of
     * the given block.
     */
    BlockState endEnergized(BlockPos pos, BlockState bs, Level world);

    static void sfx(BlockPos pos, BlockState bs, Level world, BlockEntityAbstractImpetus impetus, boolean success) {
        Vec3 vpos;
        Vec3 vecOutDir;
        FrozenPigment colorizer;

        UUID activator = Util.NIL_UUID;
        if (impetus != null && impetus.getExecutionState() != null && impetus.getExecutionState().caster != null)
            activator = impetus.getExecutionState().caster;

        if (impetus == null || impetus.getExecutionState() == null)
            colorizer = new FrozenPigment(new ItemStack(HexItems.DYE_PIGMENTS.get(DyeColor.RED)), activator);
        else
            colorizer = impetus.getPigment();

        if (bs.getBlock() instanceof BlockCircleComponent bcc) {
            var outDir = bcc.normalDir(pos, bs, world);
            var height = bcc.particleHeight(pos, bs, world);
            vecOutDir = new Vec3(outDir.step());
            vpos = Vec3.atCenterOf(pos).add(vecOutDir.scale(height));
        } else {
            // we probably are doing this because it's an error and we removed a block
            vpos = Vec3.atCenterOf(pos);
            vecOutDir = new Vec3(0, 0, 0);
        }

        if (world instanceof ServerLevel serverLevel) {
            var spray = new ParticleSpray(vpos, vecOutDir.scale(success ? 1.0 : 1.5), success ? 0.1 : 0.5,
                Mth.PI / (success ? 4 : 2), success ? 30 : 100);
            spray.sprayParticles(serverLevel,
                success ? colorizer : new FrozenPigment(new ItemStack(HexItems.DYE_PIGMENTS.get(DyeColor.RED)),
                    activator));
        }

        var pitch = 1f;
        var sound = HexSounds.SPELL_CIRCLE_FAIL;
        if (success && impetus != null) {
            sound = HexSounds.SPELL_CIRCLE_FIND_BLOCK;

            var state = impetus.getExecutionState();

            // This is a good use of my time
            var note = state.reachedPositions.size() - 1;
            var semitone = impetus.semitoneFromScale(note);
            pitch = (float) Math.pow(2.0, (semitone - 8) / 12d);
        }
        world.playSound(null, vpos.x, vpos.y, vpos.z, sound, SoundSource.BLOCKS, 1f, pitch);
    }

    /**
     * Helper function to "throw a mishap"
     */
    default void fakeThrowMishap(BlockPos pos, BlockState bs, CastingImage image, CircleCastEnv env, Mishap mishap) {
        Mishap.Context errorCtx = new Mishap.Context(null,
            bs.getBlock().getName().append(" (").append(Component.literal(pos.toShortString())).append(")"));
        var sideEffect = new OperatorSideEffect.DoMishap(mishap, errorCtx);
        var vm = new CastingVM(image, env);
        sideEffect.performEffect(vm);
    }

    abstract sealed class ControlFlow {
        public static final class Continue extends ControlFlow {
            public final CastingImage update;
            public final List<Pair<BlockPos, Direction>> exits;

            public Continue(CastingImage update, List<Pair<BlockPos, Direction>> exits) {
                this.update = update;
                this.exits = exits;
            }
        }

        public static final class Stop extends ControlFlow {
        }
    }
}
