package at.petrak.hexcasting.api.casting.eval;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.PatternShapeMatch;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.casting.mishaps.MishapBadLocation;
import at.petrak.hexcasting.api.casting.mishaps.MishapDisallowedSpell;
import at.petrak.hexcasting.api.casting.mishaps.MishapEntityTooFarAway;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.api.utils.HexUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static at.petrak.hexcasting.api.HexAPI.modLoc;
import static at.petrak.hexcasting.api.casting.eval.CastingEnvironmentComponent.*;

/**
 * Environment within which hexes are cast.
 * <p>
 * Stuff like "the player with a staff," "the player with a trinket," "spell circles,"
 */
public abstract class CastingEnvironment {
    /**
     * Stores all listeners that should be notified whenever a CastingEnvironment is initialised.
     */
    private static final List<BiConsumer<CastingEnvironment, CompoundTag>> createEventListeners = new ArrayList<>();

    /**
     * Add a listener that will be called whenever a new CastingEnvironment is created.
     */
    public static void addCreateEventListener(BiConsumer<CastingEnvironment, CompoundTag> listener) {
        createEventListeners.add(listener);
    }

    /**
     * Add a listener that will be called whenever a new CastingEnvironment is created (legacy).
     * @deprecated replaced by {@link #addCreateEventListener(BiConsumer)}
     */
    @Deprecated(since = "0.11.0-pre-660")
    public static void addCreateEventListener(Consumer<CastingEnvironment> listener) {
        createEventListeners.add((env, data) -> {listener.accept(env);});
    }

    private boolean createEventTriggered = false;

    public final void triggerCreateEvent(CompoundTag userData) {
        if (!createEventTriggered) {
            for (var listener : createEventListeners)
                listener.accept(this, userData);
            createEventTriggered = true;
        }
    }


    protected final ServerLevel world;

    protected Map<CastingEnvironmentComponent.Key<?>, @NotNull CastingEnvironmentComponent> componentMap = new HashMap<>();
    private final List<PostExecution> postExecutions = new ArrayList<>();

    private final List<PostCast> postCasts = new ArrayList<>();
    private final List<ExtractMedia.Pre> preMediaExtract = new ArrayList<>();
    private final List<ExtractMedia.Post> postMediaExtract = new ArrayList<>();

    private final List<IsVecInRange> isVecInRanges = new ArrayList<>();
    private final List<HasEditPermissionsAt> hasEditPermissionsAts = new ArrayList<>();

    protected CastingEnvironment(ServerLevel world) {
        this.world = world;
    }

    public final ServerLevel getWorld() {
        return this.world;
    }

    public int maxOpCount() {
        return HexConfig.server().maxOpCount();
    }

    /**
     * Get the caster. Might be null!
     * <p>
     * Implementations should NOT rely on this in general, use the methods on this class instead.
     * This is mostly for spells (flight, etc)
     * @deprecated as of build 0.11.1-7-pre-619 you are recommended to use {@link #getCastingEntity}
     */
    @Deprecated(since="0.11.1-7-pre-619")
    @Nullable
    public ServerPlayer getCaster() {
        return getCastingEntity() instanceof ServerPlayer sp ? sp : null;
    };

    /**
     * Gets the caster. Can be null if {@link #getCaster()} is also null
     * @return the entity casting
     */
    @Nullable
    public abstract LivingEntity getCastingEntity();

    /**
     * Get an interface used to do mishaps
     */
    public abstract MishapEnvironment getMishapEnvironment();

    public <T extends CastingEnvironmentComponent> void addExtension(@NotNull T extension) {
        componentMap.put(extension.getKey(), extension);
        if (extension instanceof PostExecution postExecution)
            postExecutions.add(postExecution);
        if (extension instanceof PostCast postCast)
            postCasts.add(postCast);
        if (extension instanceof ExtractMedia extractMedia)
            if (extension instanceof ExtractMedia.Pre pre) {
                preMediaExtract.add(pre);
            } else if (extension instanceof ExtractMedia.Post post) {
                postMediaExtract.add(post);
            }
        if (extension instanceof IsVecInRange isVecInRange)
            isVecInRanges.add(isVecInRange);
        if (extension instanceof HasEditPermissionsAt hasEditPermissionsAt)
            hasEditPermissionsAts.add(hasEditPermissionsAt);
    }

    public void removeExtension(@NotNull CastingEnvironmentComponent.Key<?> key) {
        var extension = componentMap.remove(key);
        if (extension == null)
            return;

        if (extension instanceof PostExecution postExecution)
            postExecutions.remove(postExecution);
        if (extension instanceof PostCast postCast)
            postCasts.remove(postCast);
        if (extension instanceof ExtractMedia extractMedia)
            if (extension instanceof ExtractMedia.Pre pre) {
                preMediaExtract.remove(pre);
            } else if (extension instanceof ExtractMedia.Post post) {
                postMediaExtract.remove(post);
            }
        if (extension instanceof IsVecInRange isVecInRange)
            isVecInRanges.remove(isVecInRange);
        if (extension instanceof HasEditPermissionsAt hasEditPermissionsAt)
            hasEditPermissionsAts.remove(hasEditPermissionsAt);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends CastingEnvironmentComponent> T getExtension(@NotNull CastingEnvironmentComponent.Key<T> key) {
        return (T) componentMap.get(key);
    }

    /**
     * If something about this ARE itself is invalid, mishap.
     * <p>
     * This is used for stuff like requiring enlightenment and pattern denylists
     */
    public void precheckAction(PatternShapeMatch match) throws Mishap {
        // TODO: this doesn't let you select special handlers.
        // Might be worth making a "no casting" tag on each thing
        ResourceLocation key = actionKey(match);

        if (!HexConfig.server().isActionAllowed(key)) {
            throw new MishapDisallowedSpell();
        }
    }

    @Nullable
    protected ResourceLocation actionKey(PatternShapeMatch match) {
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
        return key;
    }

    /**
     * Do whatever you like after a pattern is executed.
     */
    public void postExecution(CastResult result) {
        for (var postExecutionComponent : postExecutions)
            postExecutionComponent.onPostExecution(result);
    }

    /**
     * Do things after the whole cast is finished (i.e. every pattern to be executed has been executed).
     */
    public void postCast(CastingImage image) {
        for (var postCastComponent : postCasts)
            postCastComponent.onPostCast(image);
    }

    public abstract Vec3 mishapSprayPos();

    /**
     * Return whether this env can cast great spells.
     */
    public boolean isEnlightened() {
        var adv = this.world.getServer().getAdvancements().getAdvancement(modLoc("enlightenment"));
        if (adv == null)
            return false;

        var caster = this.getCastingEntity();
        if (caster instanceof ServerPlayer player)
            return player.getAdvancements().getOrStartProgress(adv).isDone();

        return false;
    }

    /**
     * Attempt to extract the given amount of media. Returns the amount of media left in the cost.
     * <p>
     * If there was enough media found, it will return less or equal to zero; if there wasn't, it will be
     * positive.
     */
    public long extractMedia(long cost) {
        for (var extractMediaComponent : preMediaExtract)
            cost = extractMediaComponent.onExtractMedia(cost);
        cost = extractMediaEnvironment(cost);
        for (var extractMediaComponent : postMediaExtract)
            cost = extractMediaComponent.onExtractMedia(cost);
        return cost;
    }

    /**
     * Attempt to extract the given amount of media. Returns the amount of media left in the cost.
     * <p>
     * If there was enough media found, it will return less or equal to zero; if there wasn't, it will be
     * positive.
     */
    protected abstract long extractMediaEnvironment(long cost);

    /**
     * Get if the vec is close enough, to the player or sentinel ...
     * <p>
     * Doesn't take into account being out of the <em>world</em>.
     */
    public boolean isVecInRange(Vec3 vec) {
        boolean isInRange = isVecInRangeEnvironment(vec);
        for (var isVecInRangeComponent : isVecInRanges)
            isInRange = isVecInRangeComponent.onIsVecInRange(vec, isInRange);
        return isInRange;
    }

    /**
     * Get if the vec is close enough, to the player or sentinel ...
     * <p>
     * Doesn't take into account being out of the <em>world</em>.
     */
    protected abstract boolean isVecInRangeEnvironment(Vec3 vec);

    /**
     * Return whether the caster can edit blocks at the given permission (i.e. not adventure mode, etc.)
     */
    public boolean hasEditPermissionsAt(BlockPos pos) {
        boolean hasEditPermissionsAt = hasEditPermissionsAtEnvironment(pos);
        for (var hasEditPermissionsAtComponent : hasEditPermissionsAts)
            hasEditPermissionsAt = hasEditPermissionsAtComponent.onHasEditPermissionsAt(pos, hasEditPermissionsAt);
        return hasEditPermissionsAt;
    }

    /**
     * Return whether the caster can edit blocks at the given permission (i.e. not adventure mode, etc.)
     */
    protected abstract boolean hasEditPermissionsAtEnvironment(BlockPos pos);

    public final boolean isVecInWorld(Vec3 vec) {
        return this.world.isInWorldBounds(BlockPos.containing(vec))
            && this.world.getWorldBorder().isWithinBounds(vec.x, vec.z, 0.5);
    }

    public final boolean isVecInAmbit(Vec3 vec) {
        return this.isVecInRange(vec) && this.isVecInWorld(vec);
    }

    public final boolean isEntityInRange(Entity e) {
        return (e instanceof Player && HexConfig.server().trueNameHasAmbit()) || (this.isVecInWorld(e.position()) && this.isVecInRange(e.position()));
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
        if (e instanceof ServerPlayer && HexConfig.server().trueNameHasAmbit()) {
            return;
        }
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

                if (presentCount >= count)
                    break;
            }
        }
        if (presentCount < count) {
            return false;
        }

        if (!actuallyRemove) {
            return true;
        } // Otherwise do the removal

        var remaining = count;
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
     * Attempt to replace the first stack found which matches the predicate with the stack to replace with.
     * @return whether it was successful.
     */
    public abstract boolean replaceItem(Predicate<ItemStack> stackOk, ItemStack replaceWith, @Nullable InteractionHand hand);

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
