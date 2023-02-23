package at.petrak.hexcasting.client.render;

import net.minecraft.resources.ResourceLocation;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

// *nothing* that changes can be looked at for changes
public class GaslightingTracker {
    private static int GASLIGHTING_AMOUNT = 0;
    private static int LOOKING_COOLDOWN_MAX = 40;
    private static int LOOKING_COOLDOWN = LOOKING_COOLDOWN_MAX;

    public static ResourceLocation GASLIGHTING_PRED = modLoc("variant");

    public static int getGaslightingAmount() {
        LOOKING_COOLDOWN = LOOKING_COOLDOWN_MAX;
        return GASLIGHTING_AMOUNT;
    }

    public static void postFrameCheckRendered() {
        if (LOOKING_COOLDOWN > 0) {
            LOOKING_COOLDOWN -= 1;
        } else {
            GASLIGHTING_AMOUNT += 1;
        }
    }
}
