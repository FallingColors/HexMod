package at.petrak.hexcasting.api.casting.eval.env;

import at.petrak.hexcasting.api.casting.eval.MishapEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class CircleMishapEnv extends MishapEnvironment {
    protected final BlockPos impetusLoc;
    protected final Direction startDir;

    protected CircleMishapEnv(ServerLevel world, BlockPos impetusLoc, Direction startDir) {
        super(world, null);
        this.impetusLoc = impetusLoc;
        this.startDir = startDir;
    }

    @Override
    public void yeetHeldItemsTowards(Vec3 targetPos) {

    }

    @Override
    public void dropHeldItems() {

    }

    @Override
    public void drown() {

    }

    @Override
    public void damage(float healthProportion) {

    }

    @Override
    public void removeXp(int amount) {

    }

    @Override
    public void blind(int ticks) {

    }
}
