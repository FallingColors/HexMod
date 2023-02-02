package at.petrak.hexcasting.api.casting.eval;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.casting.mishaps.MishapDisallowedSpell;
import at.petrak.hexcasting.api.casting.mishaps.MishapEntityTooFarAway;
import at.petrak.hexcasting.api.casting.mishaps.MishapLocationTooFarAway;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.utils.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Environment within which hexes are cast.
 * <p>
 * Stuff like "the player with a staff," "the player with a trinket," "spell circles,"
 */
public abstract class CastingEnvironment {
    protected final CompoundTag userData;
    protected final Set<Entity> entitiesGivenMotion;

    protected final ServerLevel world;

    protected CastingEnvironment(ServerLevel world) {
        this.userData = new CompoundTag();
        this.entitiesGivenMotion = new HashSet<>();
        this.world = world;
    }

    /**
     * Get a tag within which you can put whatever you like.
     * <p>
     * Use this to do stuff like implement custom ravenminds.
     * <p>
     * If there isn't a value associated with the given key, it makes and returns a new empty tag.
     */
    public CompoundTag getUserData(ResourceLocation key) {
        var strKey = key.toString();
        return NBTHelper.getOrCreateCompound(this.userData, strKey);
    }

    /**
     * If something about this ARE itself is invalid, mishap.
     * <p>
     * This is used for stuff like requiring enlightenment and pattern denylists
     */
    public void precheckAction(ResourceKey<ActionRegistryEntry> key) throws Mishap {
        if (!HexConfig.server().isActionAllowed(key.location())) {
            throw new MishapDisallowedSpell();
        }
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

    public final boolean isVecInWorld(Vec3 vec) {
        return this.world.isInWorldBounds(new BlockPos(vec))
            && this.world.getWorldBorder().isWithinBounds(vec.x, vec.z, 0.5);
    }

    public final boolean isVecInAmbit(Vec3 vec) {
        return this.isVecInRange(vec) && this.isVecInWorld(vec);
    }

    public final boolean isEntityInAmbit(Entity e) {
        return this.isVecInRange(e.position());
    }

    /**
     * Convenience function to throw if the vec is out of the caster's range or the world
     */
    public final void assertVecInAmbit(Vec3 vec) throws MishapLocationTooFarAway {
        this.assertVecInWorld(vec);
        if (this.isVecInRange(vec)) {
            throw new MishapLocationTooFarAway(vec, "too_far");
        }
    }

    /**
     * Convenience function to throw if the entity is out of the caster's range or the world
     */
    public final void assertEntityInAmbit(Entity e) throws MishapEntityTooFarAway {
        if (!this.isVecInWorld(e.position())) {
            throw new MishapEntityTooFarAway(e);
        }
        if (this.isVecInRange(e.position())) {
            throw new MishapEntityTooFarAway(e);
        }
    }

    /**
     * Convenience function to throw if the vec is out of the world (for GTP)
     */
    public final void assertVecInWorld(Vec3 vec) throws MishapLocationTooFarAway {
        if (!this.isVecInWorld(vec)) {
            throw new MishapLocationTooFarAway(vec, "out_of_world");
        }
    }

    public void markEntityAsImpulsed(Entity e) {
        this.entitiesGivenMotion.add(e);
    }

    public boolean hasEntityBeenImpulsed(Entity e) {
        return this.entitiesGivenMotion.contains(e);
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

    /**
     * Attempt to withdraw some number of items from stacks available.
     * <p>
     * Return whether it was successful.
     */
    public boolean withdrawItem(Predicate<ItemStack> stackOk, int count, boolean actuallyRemove) {
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
    public enum StackDiscoveryMode {
        /**
         * When finding items to pick (hotbar)
         */
        QUERY,
        /**
         * When extracting things
         */
        EXTRACTION,
    }
}
