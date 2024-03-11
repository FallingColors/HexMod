package at.petrak.hexcasting.api.casting.eval;

import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public interface CastingEnvironmentComponent {
    Key<?> getKey();

    interface Key<C extends CastingEnvironmentComponent> {}

    interface PostExecution extends CastingEnvironmentComponent {
        /**
         * Do whatever you like after a pattern is executed.
         */
        void onPostExecution(CastResult result);
    }

    interface PostCast extends CastingEnvironmentComponent {
        /**
         * Do things after the whole cast is finished (i.e. every pattern to be executed has been executed).
         */
        void onPostCast(CastingImage image);
    }

    interface ExtractMedia extends CastingEnvironmentComponent {
        /**
         * Receives the cost that is being extracted, should return the
         * remaining cost after deducting whatever cost source this component
         * is responsible for (should be >= 0). All Components are executed
         * before the CastingEnvironment's extractMedia is executed.
         */
        long onExtractMedia(long cost);
    }

    interface IsVecInRange extends CastingEnvironmentComponent {
        /**
         * Receives the vec, and the current return value, and returns the new return value.
         */
        boolean onIsVecInRange(Vec3 vec, boolean current);
    }

    interface HasEditPermissionsAt extends CastingEnvironmentComponent {
        /**
         * Receives the vec, and the current return value, and returns the new return value.
         */
        boolean onHasEditPermissionsAt(BlockPos pos, boolean current);
    }
}
