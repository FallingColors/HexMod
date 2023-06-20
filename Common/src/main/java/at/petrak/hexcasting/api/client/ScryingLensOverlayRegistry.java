package at.petrak.hexcasting.api.client;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Vector;
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

    /**
     * Add the block to display things when the player is holding a lens and looking at it.
     *
     * @throws IllegalArgumentException if the block is already registered.
     */
    public static void addDisplayer(Block block, OverlayBuilder displayer) {
        addDisplayer(BuiltInRegistries.BLOCK.getKey(block), displayer);
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
        Player observer, Level world,
        Direction hitFace) {
        List<Pair<ItemStack, Component>> lines = Lists.newArrayList();
        var idLookedup = ID_LOOKUP.get(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
        if (idLookedup != null) {
            idLookedup.addLines(lines, state, pos, observer, world, hitFace);
        }

        for (var pair : PREDICATE_LOOKUP) {
            if (pair.getFirst().test(state, pos, observer, world, hitFace)) {
                pair.getSecond().addLines(lines, state, pos, observer, world, hitFace);
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
            BlockState state, BlockPos pos, Player observer,
            Level world,
            Direction hitFace);
    }

    /**
     * Predicate for matching on a block state.
     */
    @FunctionalInterface
    public interface OverlayPredicate {
        boolean test(BlockState state, BlockPos pos, Player observer,
            Level world,
            Direction hitFace);
    }
}
