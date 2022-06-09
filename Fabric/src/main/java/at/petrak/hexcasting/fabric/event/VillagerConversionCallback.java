package at.petrak.hexcasting.fabric.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.LivingEntity;

// https://fabricmc.net/wiki/tutorial:events

/**
 * Callback for when a LivingEntity turns into another entity, like a villager being struck by lightning.
 * This event is fired after the conversion happens and cannot be cancelled.
 */
@FunctionalInterface
public interface VillagerConversionCallback {
    Event<VillagerConversionCallback> EVENT = EventFactory.createArrayBacked(VillagerConversionCallback.class,
        listeners -> (original, outcome) -> {
            for (var cb : listeners) {
                cb.interact(original, outcome);
            }
        });

    void interact(LivingEntity original, LivingEntity outcome);
}
