package at.petrak.hexcasting.client;

public class ClientTickCounter {
    public static long ticksInGame = 0L;
    public static float partialTicks = 0.0F;
    public static float delta = 0.0F;
    public static float total = 0.0F;

    public static void renderTickStart(float renderTickTime) {
        partialTicks = renderTickTime;
    }

    public static void renderTickEnd() {
        calcDelta();
    }

    public static void clientTickEnd() {
        ++ticksInGame;
        partialTicks = 0.0F;
        calcDelta();
    }

    private static void calcDelta() {
        float oldTotal = total;
        total = (float)ticksInGame + partialTicks;
        delta = total - oldTotal;
    }
}
