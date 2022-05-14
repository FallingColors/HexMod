package at.petrak.hexcasting.api.client;

import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Use this to make things display when the player looks at things with a Scrying Lens.
 * <p>
 * Client-side only.
 */
public final class ScryingLensOverlayRegistry {
    private static final ConcurrentMap<ResourceLocation, OverlayBuilder> ID_LOOKUP = new ConcurrentHashMap<>();
    // vectors are thread-safe!
    private static final List<Pair<OverlayPredicate, OverlayBuilder>> PREDICATE_LOOKUP = new Vector<>();

    // implemented as a map to allow for weak dereferencing
    private static final Map<LocalPlayer, Pair<BlockPos, Integer>> comparatorData = new WeakHashMap<>();

    public static void receiveComparatorValue(BlockPos pos, int value) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            if (pos == null || value == -1) {
                comparatorData.remove(player);
            } else {
                comparatorData.put(player, new Pair<>(pos, value));
            }
        }
    }

    public static int getComparatorValue(boolean onlyRealComparators) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        var level = mc.level;
        var result = mc.hitResult;

        if (player == null || level == null || result == null || result.getType() != HitResult.Type.BLOCK) {
            return -1;
        }

        var comparatorValue = comparatorData.get(player);
        if (comparatorValue == null) {
            return -1;
        }

        var pos = ((BlockHitResult) result).getBlockPos();
        if (!pos.equals(comparatorValue.getFirst())) {
            return -1;
        }

        var state = mc.level.getBlockState(pos);
        if ((onlyRealComparators && !state.is(
            Blocks.COMPARATOR)) || (!onlyRealComparators && !state.hasAnalogOutputSignal())) {
            return -1;
        }

        return comparatorValue.getSecond();
    }

    /**
     * Add the block to display things when the player is holding a lens and looking at it.
     *
     * @throws IllegalArgumentException if the block is already registered.
     */
    public static void addDisplayer(Block block, OverlayBuilder displayer) {
        addDisplayer(IXplatAbstractions.INSTANCE.getID(block), displayer);
    }

    /**
     * Add the block to display things when the player is holding a lens and looking at it.
     *
     * @throws IllegalArgumentException if the block ID is already registered.
     */
    public static void addDisplayer(ResourceLocation blockID, OverlayBuilder displayer) {
        if (ID_LOOKUP.containsKey(blockID)) {
            throw new IllegalArgumentException("Already have a displayer for " + blockID);
        }
        ID_LOOKUP.put(blockID, displayer);
    }

    /**
     * Display things when the player is holding a lens and looking at some block via a predicate.
     * <p>
     * These have a lower priority than the standard ID-based displays, so if an ID and predicate both match,
     * this won't be displayed.
     */
    public static void addPredicateDisplayer(OverlayPredicate predicate, OverlayBuilder displayer) {
        PREDICATE_LOOKUP.add(new Pair<>(predicate, displayer));
    }

    /**
     * Internal use only.
     */
    public static @NotNull List<Pair<ItemStack, Component>> getLines(BlockState state, BlockPos pos,
        LocalPlayer observer, ClientLevel world,
        Direction hitFace, @Nullable InteractionHand lensHand) {
        List<Pair<ItemStack, Component>> lines = Lists.newArrayList();
        var idLookedup = ID_LOOKUP.get(IXplatAbstractions.INSTANCE.getID(state.getBlock()));
        if (idLookedup != null) {
            idLookedup.addLines(lines, state, pos, observer, world, hitFace, lensHand);
        }

        for (var pair : PREDICATE_LOOKUP) {
            if (pair.getFirst().test(state, pos, observer, world, hitFace, lensHand)) {
                pair.getSecond().addLines(lines, state, pos, observer, world, hitFace, lensHand);
            }
        }

        return lines;
    }

    /**
     * Return the lines displayed by the cursor: an item and some text.
     * <p>
     * The ItemStack can be empty; if it is, the text isn't shifted over for it.
     */
    @FunctionalInterface
    public interface OverlayBuilder {
        void addLines(List<Pair<ItemStack, Component>> lines,
            BlockState state, BlockPos pos, LocalPlayer observer,
            ClientLevel world,
            Direction hitFace, @Nullable InteractionHand lensHand);
    }

    /**
     * Predicate for matching on a block state.
     */
    @FunctionalInterface
    public interface OverlayPredicate {
        boolean test(BlockState state, BlockPos pos, LocalPlayer observer,
            ClientLevel world,
            Direction hitFace, @Nullable InteractionHand lensHand);
    }
}
