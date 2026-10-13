package at.petrak.hexcasting.api.client;

import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

// *nothing* that changes can be looked at for changes
public class GaslightingTracker {
    public static Map<ResourceLocation, GaslightingTracker> GASLIGHTING_TRACKERS = new LinkedHashMap<>();
    private int GASLIGHTING_AMOUNT = 0;
    private final int LOOKING_COOLDOWN_MAX;
    private int LOOKING_COOLDOWN;

    public ResourceLocation GASLIGHTING_PRED;

    public GaslightingTracker(ResourceLocation resLoc, int maxCooldown) {
        this.GASLIGHTING_PRED = resLoc;
        this.LOOKING_COOLDOWN_MAX = maxCooldown;
        this.LOOKING_COOLDOWN = LOOKING_COOLDOWN_MAX;
        GaslightingTracker.GASLIGHTING_TRACKERS.put(this.GASLIGHTING_PRED, this);
    }

    public int getGaslightingAmount() {
        LOOKING_COOLDOWN = LOOKING_COOLDOWN_MAX;
        return GASLIGHTING_AMOUNT;
    }

    public void postFrameCheckRendered() {
        if (LOOKING_COOLDOWN > 0) {
            LOOKING_COOLDOWN -= 1;
        } else {
            GASLIGHTING_AMOUNT += 1;
        }
    }
}