package at.petrak.hexcasting.api.client;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Use this to make things display when the player looks at things with a Scrying Lens.
 * <p>
 * Client-side only.
 */
public class ScryingLensOverlayRegistry {
    private static final ConcurrentMap<ResourceLocation, Displayer> BLOCK_LOOKUP = new ConcurrentHashMap<>();

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
        if (BLOCK_LOOKUP.containsKey(blockID)) {
            throw new IllegalArgumentException("Already have a displayer for " + blockID);
        }
        BLOCK_LOOKUP.put(blockID, displayer);
    }

    /**
     * Add the block to display things when the player is holding a lens and looking at it.
     * <p>
     * If the block ID is already registered, append this {@link Displayer}'s output after whatever
     * was previously there.
     * <p>
     * Returns if there was already something in the map (and this is appending).
     */
    public static boolean appendDisplayer(ResourceLocation blockID, Displayer displayer) {
        var extant = BLOCK_LOOKUP.getOrDefault(blockID, null);
        if (extant == null) {
            BLOCK_LOOKUP.put(blockID, displayer);
            return false;
        } else {
            var andThen = new AndThen(extant, displayer);
            BLOCK_LOOKUP.put(blockID, andThen);
            return true;
        }
    }

    /**
     * Internal use only.
     */
    public static @Nullable Displayer getDisplayer(Block block) {
        return BLOCK_LOOKUP.getOrDefault(block.getRegistryName(), null);
    }

    /**
     * Return the lines displayed by the cursor: an item and some text.
     * <p>
     * The ItemStack can be null; if it is, the text isn't shifted over for it.
     */
    @FunctionalInterface
    public interface Displayer {
        List<Pair<ItemStack, Component>> getLines(BlockState state, BlockPos pos, LocalPlayer observer,
            InteractionHand lensHand);
    }

    private record AndThen(Displayer first, Displayer then) implements Displayer {
        @Override
        public List<Pair<ItemStack, Component>> getLines(BlockState state, BlockPos pos,
            LocalPlayer observer, InteractionHand lensHand) {
            var lines = this.first.getLines(state, pos, observer, lensHand);
            var mutableLines = new ArrayList<>(lines);
            mutableLines.addAll(this.then.getLines(state, pos, observer, lensHand));
            return mutableLines;
        }
    }
}
