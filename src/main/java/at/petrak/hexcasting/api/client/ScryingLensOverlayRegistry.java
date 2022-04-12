package at.petrak.hexcasting.api.client;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Use this to make things display when the player looks at things with a Scrying Lens.
 * <p>
 * Client-side only.
 */
@OnlyIn(Dist.CLIENT)
public final class ScryingLensOverlayRegistry {
    private static final ConcurrentMap<ResourceLocation, Displayer> ID_LOOKUP = new ConcurrentHashMap<>();
    // vectors are thread-safe!
    private static final List<Pair<Predicate, Displayer>> PREDICATE_LOOKUP = new Vector<>();

    /**
     * Add the block to display things when the player is holding a lens and looking at it.
     *
     * @throws IllegalArgumentException if the block is already registered.
     */
    public static void addDisplayer(Block block, Displayer displayer) {
        addDisplayer(block.getRegistryName(), displayer);
    }

    /**
     * Add the block to display things when the player is holding a lens and looking at it.
     *
     * @throws IllegalArgumentException if the block ID is already registered.
     */
    public static void addDisplayer(ResourceLocation blockID, Displayer displayer) {
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
    public static void addPredicateDisplayer(Predicate predicate, Displayer displayer) {
        PREDICATE_LOOKUP.add(new Pair<>(predicate, displayer));
    }

    /**
     * Internal use only.
     */
    public static @Nullable List<Pair<ItemStack, Component>> getLines(BlockState state, BlockPos pos,
        LocalPlayer observer, ClientLevel world,
        @Nullable InteractionHand lensHand) {
        var idLookedup = ID_LOOKUP.get(state.getBlock().getRegistryName());
        if (idLookedup != null) {
            return idLookedup.getLines(state, pos, observer, world, lensHand);
        }

        for (var pair : PREDICATE_LOOKUP) {
            if (pair.getFirst().test(state, pos, observer, world, lensHand)) {
                return pair.getSecond().getLines(state, pos, observer, world, lensHand);
            }
        }

        return null;
    }

    /**
     * Return the lines displayed by the cursor: an item and some text.
     * <p>
     * The ItemStack can be null; if it is, the text isn't shifted over for it.
     */
    @FunctionalInterface
    public interface Displayer {
        List<Pair<ItemStack, Component>> getLines(BlockState state, BlockPos pos, LocalPlayer observer,
            ClientLevel world,
            @Nullable InteractionHand lensHand);
    }

    /**
     * Predicate for matching on a block state.
     */
    @FunctionalInterface
    public interface Predicate {
        boolean test(BlockState state, BlockPos pos, LocalPlayer observer, ClientLevel world, @Nullable InteractionHand lensHand);
    }
}
