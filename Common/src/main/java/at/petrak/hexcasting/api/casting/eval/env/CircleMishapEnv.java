package at.petrak.hexcasting.api.casting.eval.env;

import at.petrak.hexcasting.api.casting.eval.MishapEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CircleMishapEnv extends MishapEnvironment {
    protected final BlockPos impetusLoc;
    protected final Direction startDir;
    protected final @Nullable ServerPlayer caster;
    protected final AABB bounds;

    protected CircleMishapEnv(ServerLevel world, BlockPos impetusLoc, Direction startDir, @Nullable ServerPlayer caster, AABB bounds) {
        super(world, null);
        this.impetusLoc = impetusLoc;
        this.startDir = startDir;
        this.caster = caster;
        this.bounds = bounds;
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
