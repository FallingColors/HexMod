package at.petrak.hexcasting.client;

import at.petrak.hexcasting.api.client.GaslightingTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class ClientTickCounter {
    public static long ticksInGame = 0L;
    public static float partialTicks = 0.0F;

    public static float getTotal() {
        return (float) ticksInGame + partialTicks;
    }

    public static void renderTickStart(float renderTickTime) {
        partialTicks = renderTickTime;
    }

    public static void clientTickEnd() {
        if (!Minecraft.getInstance().isPaused()) {
            ++ticksInGame;
            partialTicks = 0.0F;
            for (Map.Entry<ResourceLocation, GaslightingTracker> entry : GaslightingTracker.GASLIGHTING_TRACKERS.entrySet()) {
                entry.getValue().postFrameCheckRendered();
            }
        }
    }
}
