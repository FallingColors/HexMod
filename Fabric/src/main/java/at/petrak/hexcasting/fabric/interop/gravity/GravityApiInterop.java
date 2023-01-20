package at.petrak.hexcasting.fabric.interop.gravity;

import at.petrak.hexcasting.common.casting.PatternRegistryManifest;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class GravityApiInterop {
    public static void init() {
        try {
            PatternRegistryManifest.mapPattern(HexPattern.fromAngles("wawawddew", HexDir.NORTH_EAST),
                modLoc("interop/gravity/get"), OpGetGravity.INSTANCE);
            PatternRegistryManifest.mapPattern(HexPattern.fromAngles("wdwdwaaqw", HexDir.NORTH_WEST),
                modLoc("interop/gravity/set"), OpChangeGravity.INSTANCE);
        } catch (PatternRegistryManifest.RegisterPatternException e) {
            e.printStackTrace();
        }
    }
}
