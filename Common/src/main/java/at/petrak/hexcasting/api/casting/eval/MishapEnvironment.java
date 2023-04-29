package at.petrak.hexcasting.api.casting.eval;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Kinda like {@link CastingEnvironment} but for executing mishaps.
 * <p>
 * To avoid horrible O(mn) scope problems we offer a set of stock bad effects.
 * The player is exposed nullably if you like though.
 */
public abstract class MishapEnvironment {
    @Nullable
    protected final ServerPlayer caster;
    protected final ServerLevel world;

    protected MishapEnvironment(ServerLevel world, @Nullable ServerPlayer caster) {
        this.caster = caster;
        this.world = world;
    }

    public abstract void yeetHeldItemsTowards(Vec3 targetPos);

    public abstract void dropHeldItems();

    public abstract void drown();

    public abstract void damage(float healthProportion);

    public abstract void removeXp(int amount);

    public abstract void blind(int ticks);

    protected void yeetItem(ItemStack stack, Vec3 srcPos, Vec3 delta) {
        var entity = new ItemEntity(
            this.world,
            srcPos.x, srcPos.y, srcPos.z,
            stack,
            delta.x + (Math.random() - 0.5) * 0.1,
            delta.y + (Math.random() - 0.5) * 0.1,
            delta.z + (Math.random() - 0.5) * 0.1
        );
        entity.setPickUpDelay(40);
        this.world.addWithUUID(entity);
    }
}
