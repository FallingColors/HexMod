package at.petrak.hexcasting.client;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientTickCounter {
    public static long ticksInGame = 0L;
    public static float partialTicks = 0.0F;
    public static float delta = 0.0F;
    public static float total = 0.0F;

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent evt) {
        if (evt.phase == TickEvent.Phase.START) {
            partialTicks = evt.renderTickTime;
        } else {
            calcDelta();
        }
    }
    @SubscribeEvent
    public static void onTickEnd(TickEvent.ClientTickEvent evt) {
        if (evt.phase == TickEvent.Phase.END) {
            ++ticksInGame;
            partialTicks = 0.0F;
            calcDelta();
        }
    }

    private static void calcDelta() {
        float oldTotal = total;
        total = (float)ticksInGame + partialTicks;
        delta = total - oldTotal;
    }
}
