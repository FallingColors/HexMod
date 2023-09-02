package at.petrak.hexcasting.client;

import net.minecraft.client.Minecraft;

public class ClientTickCounter {
    public static long ticksInGame = 0L;
    public static float partialTicks = 0.0F;

    public static float getTotal() {
        return (float)ticksInGame + partialTicks;
    }

    public static void renderTickStart(float renderTickTime) {
        partialTicks = renderTickTime;
    }

    public static void clientTickEnd() {
        if (!Minecraft.getInstance().isPaused()) {
            ++ticksInGame;
            partialTicks = 0.0F;
        }
    }
}
