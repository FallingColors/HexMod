package at.petrak.hexcasting.api.misc;

import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import at.petrak.hexcasting.api.spell.casting.CastingContext;
import at.petrak.hexcasting.api.spell.casting.CastingHarness;
import com.google.common.collect.Lists;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class DiscoveryHandlers {
    private static final List<Predicate<Player>> HAS_LENS_PREDICATE = new ArrayList<>();
    private static final List<Function<CastingHarness, List<ADMediaHolder>>> MEDIA_HOLDER_DISCOVERY = new ArrayList<>();
    private static final List<FunctionToFloat<Player>> GRID_SCALE_MODIFIERS = new ArrayList<>();
    private static final List<Function<CastingContext, List<ItemStack>>> ITEM_SLOT_DISCOVERER = new ArrayList<>();
    private static final List<Function<CastingContext, List<ItemStack>>> OPERATIVE_SLOT_DISCOVERER = new ArrayList<>();
    private static final List<BiFunction<Player, String, ItemStack>> DEBUG_DISCOVERER = new ArrayList<>();

    public static boolean hasLens(Player player) {
        for (var predicate : HAS_LENS_PREDICATE) {
            if (predicate.test(player)) {
                return true;
            }
        }
        return false;
    }

    public static List<ADMediaHolder> collectMediaHolders(CastingHarness harness) {
        List<ADMediaHolder> holders = Lists.newArrayList();
        for (var discoverer : MEDIA_HOLDER_DISCOVERY) {
            holders.addAll(discoverer.apply(harness));
        }
        return holders;
    }

    public static float gridScaleModifier(Player player) {
        float mod = 1;
        for (var modifier : GRID_SCALE_MODIFIERS) {
            mod *= modifier.apply(player);
        }
        return mod;
    }

    public static List<ItemStack> collectItemSlots(CastingContext ctx) {
        List<ItemStack> stacks = Lists.newArrayList();
        for (var discoverer : ITEM_SLOT_DISCOVERER) {
            stacks.addAll(discoverer.apply(ctx));
        }
        return stacks;
    }

    public static List<ItemStack> collectOperableSlots(CastingContext ctx) {
        List<ItemStack> stacks = Lists.newArrayList();
        for (var discoverer : OPERATIVE_SLOT_DISCOVERER) {
            stacks.addAll(discoverer.apply(ctx));
        }
        return stacks;
    }

    public static ItemStack findDebugItem(Player player, String type) {
        for (var discoverer : DEBUG_DISCOVERER) {
            var stack = discoverer.apply(player, type);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static void addLensPredicate(Predicate<Player> predicate) {
        HAS_LENS_PREDICATE.add(predicate);
    }

    public static void addMediaHolderDiscoverer(Function<CastingHarness, List<ADMediaHolder>> discoverer) {
        MEDIA_HOLDER_DISCOVERY.add(discoverer);
    }

    public static void addGridScaleModifier(FunctionToFloat<Player> modifier) {
        GRID_SCALE_MODIFIERS.add(modifier);
    }

    public static void addItemSlotDiscoverer(Function<CastingContext, List<ItemStack>> discoverer) {
        ITEM_SLOT_DISCOVERER.add(discoverer);
    }

    public static void addOperativeSlotDiscoverer(Function<CastingContext, List<ItemStack>> discoverer) {
        OPERATIVE_SLOT_DISCOVERER.add(discoverer);
    }

    public static void addDebugItemDiscoverer(BiFunction<Player, String, ItemStack> discoverer) {
        DEBUG_DISCOVERER.add(discoverer);
    }

    @FunctionalInterface
    public interface FunctionToFloat<T> {
        float apply(T t);
    }
}
