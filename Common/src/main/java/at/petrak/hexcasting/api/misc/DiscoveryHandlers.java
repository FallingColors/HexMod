package at.petrak.hexcasting.api.misc;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DiscoveryHandlers {
    private static final List<BiFunction<Player, String, ItemStack>> DEBUG_DISCOVERER = new ArrayList<>();
    private static final List<Function<Player, List<ItemStack>>> EXTRA_EQUIPMENT_DISCOVERY = new ArrayList<>();

    public static ItemStack findDebugItem(Player player, String type) {
        for (var discoverer : DEBUG_DISCOVERER) {
            var stack = discoverer.apply(player, type);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static List<ItemStack> collectExtraEquipments(Player player) {
        List<ItemStack> stacks = new ArrayList<>();
        for (var discoverer : EXTRA_EQUIPMENT_DISCOVERY) {
            stacks.addAll(discoverer.apply(player));
        }
        return stacks;
    }

    public static void addDebugItemDiscoverer(BiFunction<Player, String, ItemStack> discoverer) {
        DEBUG_DISCOVERER.add(discoverer);
    }

    public static void addExtraEquipmentDiscoverer(Function<Player, List<ItemStack>> discoverer) {
        EXTRA_EQUIPMENT_DISCOVERY.add(discoverer);
    }
}
