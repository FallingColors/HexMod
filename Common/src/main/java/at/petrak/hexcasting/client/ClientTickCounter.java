package at.petrak.hexcasting.client;

// I can't find a better way to do this :(
public class ClientTickCounter {
    private static long tickCount = 0;

    public static void onTick() {
        tickCount++;
    }

    public static long getTickCount() {
        return tickCount;
    }
}
