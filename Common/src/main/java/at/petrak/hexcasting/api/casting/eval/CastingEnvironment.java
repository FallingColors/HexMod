package at.petrak.hexcasting.api.casting.eval;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.PatternShapeMatch;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.casting.mishaps.MishapBadLocation;
import at.petrak.hexcasting.api.casting.mishaps.MishapDisallowedSpell;
import at.petrak.hexcasting.api.casting.mishaps.MishapEntityTooFarAway;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.api.utils.HexUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Environment within which hexes are cast.
 * <p>
 * Stuff like "the player with a staff," "the player with a trinket," "spell circles,"
 */
public abstract class CastingEnvironment {
    protected final ServerLevel world;

    protected CastingEnvironment(ServerLevel world) {
        this.world = world;
    }

    public final ServerLevel getWorld() {
        return this.world;
    }

    /**
     * Get the caster. Might be null!
     * <p>
     * Implementations should NOT rely on this in general, use the methods on this class instead.
     * This is mostly for spells (flight, etc)
     */
    @Nullable
    public abstract ServerPlayer getCaster();

    /**
     * Get an interface used to do mishaps
     */
    public abstract MishapEnvironment getMishapEnvironment();

    /**
     * If something about this ARE itself is invalid, mishap.
     * <p>
     * This is used for stuff like requiring enlightenment and pattern denylists
     */
    public void precheckAction(PatternShapeMatch match) throws Mishap {
        // TODO: this doesn't let you select special handlers.
        // Might be worth making a "no casting" tag on each thing
        ResourceLocation key;
        if (match instanceof PatternShapeMatch.Normal normal) {
            key = normal.key.location();
        } else if (match instanceof PatternShapeMatch.PerWorld perWorld) {
            key = perWorld.key.location();
        } else if (match instanceof PatternShapeMatch.Special special) {
            key = special.key.location();
        } else {
            key = null;
        }
        if (!HexConfig.server().isActionAllowed(key)) {
            throw new MishapDisallowedSpell();
        }
    }

    /**
     * Do whatever you like after a pattern is executed.
     */
    public abstract void postExecution(CastResult result);

    public abstract Vec3 mishapSprayPos();

    /**
     * Return whether this env can cast great spells.
     */
    public boolean isEnlightened() {
        return false;
    }

    /**
     * Attempt to extract the given amount of media. Returns the amount of media left in the cost.
     * <p>
     * If there was enough media found, it will return less or equal to zero; if there wasn't, it will be
     * positive.
     */
    public abstract long extractMedia(long cost);

    /**
     * Get if the vec is close enough, to the player or sentinel ...
     * <p>
     * Doesn't take into account being out of the <em>world</em>.
     */
    public abstract boolean isVecInRange(Vec3 vec);

    /**
     * Return whether the caster can edit blocks at the given permission (i.e. not adventure mode, etc.)
     */
    public abstract boolean hasEditPermissionsAt(BlockPos vec);

    public final boolean isVecInWorld(Vec3 vec) {
        return this.world.isInWorldBounds(new BlockPos(vec))
            && this.world.getWorldBorder().isWithinBounds(vec.x, vec.z, 0.5);
    }

    public final boolean isVecInAmbit(Vec3 vec) {
        return this.isVecInRange(vec) && this.isVecInWorld(vec);
    }

    public final boolean isEntityInRange(Entity e) {
        return this.isVecInRange(e.position());
    }

    /**
     * Convenience function to throw if the vec is out of the caster's range or the world
     */
    public final void assertVecInRange(Vec3 vec) throws MishapBadLocation {
        this.assertVecInWorld(vec);
        if (!this.isVecInRange(vec)) {
            throw new MishapBadLocation(vec, "too_far");
        }
    }

    public final void assertPosInRange(BlockPos vec) throws MishapBadLocation {
        this.assertVecInRange(new Vec3(vec.getX(), vec.getY(), vec.getZ()));
    }

    public final void assertPosInRangeForEditing(BlockPos vec) throws MishapBadLocation {
        this.assertVecInRange(new Vec3(vec.getX(), vec.getY(), vec.getZ()));
        if (!this.canEditBlockAt(vec))
            throw new MishapBadLocation(Vec3.atCenterOf(vec), "forbidden");
    }

    public final boolean canEditBlockAt(BlockPos vec) {
        return this.isVecInRange(Vec3.atCenterOf(vec)) && this.hasEditPermissionsAt(vec);
    }

    /**
     * Convenience function to throw if the entity is out of the caster's range or the world
     */
    public final void assertEntityInRange(Entity e) throws MishapEntityTooFarAway {
        if (!this.isVecInWorld(e.position())) {
            throw new MishapEntityTooFarAway(e);
        }
        if (!this.isVecInRange(e.position())) {
            throw new MishapEntityTooFarAway(e);
        }
    }

    /**
     * Convenience function to throw if the vec is out of the world (for GTP)
     */
    public final void assertVecInWorld(Vec3 vec) throws MishapBadLocation {
        if (!this.isVecInWorld(vec)) {
            throw new MishapBadLocation(vec, "out_of_world");
        }
    }

    public abstract InteractionHand getCastingHand();

    public InteractionHand getOtherHand() {
        return HexUtils.otherHand(this.getCastingHand());
    }

    /**
     * Get the item in the "other hand."
     * <p>
     * If that hand is empty, or if they cannot have that hand, return Empty.
     * Probably return a clone of Empty, actually...
     */
    public abstract ItemStack getAlternateItem();

    /**
     * Get all the item stacks this env can use.
     */
    protected abstract List<ItemStack> getUsableStacks(StackDiscoveryMode mode);

    /**
     * Get the primary/secondary item stacks this env can use (i.e. main hand and offhand for the player).
     */
    protected abstract List<HeldItemInfo> getPrimaryStacks();

    /**
     * Return the slot from which to take blocks and items.
     */
    @Nullable
    public ItemStack queryForMatchingStack(Predicate<ItemStack> stackOk) {
        var stacks = this.getUsableStacks(StackDiscoveryMode.QUERY);
        for (ItemStack stack : stacks) {
            if (stackOk.test(stack)) {
                return stack;
            }
        }

        return null;
    }

    public record HeldItemInfo(ItemStack stack, @Nullable InteractionHand hand) {
        public ItemStack component1() {
            return stack;
        }

        public @Nullable InteractionHand component2() {
            return hand;
        }
    }

    /**
     * Return the slot from which to take blocks and items.
     */
    // TODO winfy: resolve the null here
    public @Nullable HeldItemInfo getHeldItemToOperateOn(Predicate<ItemStack> stackOk) {
        var stacks = this.getPrimaryStacks();
        for (HeldItemInfo stack : stacks) {
            if (stackOk.test(stack.stack)) {
                return stack;
            }
        }

        return null;
    }

    /**
     * Whether to provide infinite items.
     */
    protected boolean isCreativeMode() {
        return false;
    }

    /**
     * Attempt to withdraw some number of items from stacks available.
     * <p>
     * Return whether it was successful.
     */
    public boolean withdrawItem(Predicate<ItemStack> stackOk, int count, boolean actuallyRemove) {
        if (this.isCreativeMode()) {
            return true;
        }

        var stacks = this.getUsableStacks(StackDiscoveryMode.EXTRACTION);

        var presentCount = 0;
        var matches = new ArrayList<ItemStack>();
        for (ItemStack stack : stacks) {
            if (stackOk.test(stack)) {
                presentCount += stack.getCount();
                matches.add(stack);
            }
        }
        if (presentCount < count) {
            return false;
        }

        if (!actuallyRemove) {
            return true;
        } // Otherwise do the removal

        var remaining = presentCount;
        for (ItemStack match : matches) {
            var toWithdraw = Math.min(match.getCount(), remaining);
            match.shrink(toWithdraw);

            remaining -= toWithdraw;
            if (remaining <= 0) {
                return true;
            }
        }

        throw new IllegalStateException("unreachable");
    }

    /**
     * The order/mode stacks should be discovered in
     */
    protected enum StackDiscoveryMode {
        /**
         * When finding items to pick (hotbar)
         */
        QUERY,
        /**
         * When extracting things
         */
        EXTRACTION,
    }

    public abstract FrozenPigment getPigment();

    public abstract @Nullable FrozenPigment setPigment(@Nullable FrozenPigment pigment);

    public abstract void produceParticles(ParticleSpray particles, FrozenPigment colorizer);

    public abstract void printMessage(Component message);
}
