package at.petrak.hexcasting.common.lib;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class HexItemHolderHandlers {
    private static Map<EntityType<?>, Function<? extends Entity, ItemStack>> REGISTRY = new HashMap<>();

    /**
     * Register a handler for mapping an entity type to an item.
     *
     * @param type    Type of entity to register the handler for.
     * @param handler The handler function. May return null, in which case dependent patterns will mishap.
     */
    public static <T extends Entity> void register(EntityType<T> type, Function<T, ItemStack> handler) {
        REGISTRY.put(type, handler);
    }

    @SuppressWarnings("unchecked")
    public static @Nullable ItemStack applyHandlerFor(Entity e) {
        var handler = REGISTRY.getOrDefault(e.getType(), entity -> null);
        ItemStack stack = ((Function<Entity, ItemStack>) handler).apply(e);

        if (stack != null && stack.isEmpty()) stack = null;

        return stack;
    }

    public static void init() {
        register(EntityType.ITEM, ItemEntity::getItem);
        register(EntityType.ITEM_FRAME, ItemFrame::getItem);
        register(EntityType.PLAYER, player -> {
            ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!mainHand.isEmpty()) return mainHand;
            return player.getItemInHand(InteractionHand.OFF_HAND);
        });
    }
}
