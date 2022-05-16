package at.petrak.hexcasting.fabric.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Return true to cancel any further processing of the mouse scroll, false to keep going.
 */
@FunctionalInterface
public interface MouseScrollCallback {
    Event<MouseScrollCallback> EVENT = EventFactory.createArrayBacked(MouseScrollCallback.class,
        listeners -> (delta) -> {
            for (var cb : listeners) {
                var cancel = cb.interact(delta);
                if (cancel) {
                    return true;
                }
            }
            return false;
        });

    boolean interact(double delta);
}
