package at.petrak.hexcasting.client;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// I can't find a better way to do this :(
public class ClientTickCounter {
    private static long tickCount = 0;

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent evt) {
        tickCount++;
    }

    public static long getTickCount() {
        return tickCount;
    }
}
