package at.petrak.hexcasting.common.lib.hex;

import at.petrak.hexcasting.api.client.GaslightingTracker;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexGaslighting {
    public static void init() {}

    public static GaslightingTracker QUENCHED_ALLAY_GASLIGHTING = new GaslightingTracker(modLoc("quenched_allay_variants"), 40);

}
