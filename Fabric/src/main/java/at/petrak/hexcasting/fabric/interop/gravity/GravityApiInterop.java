package at.petrak.hexcasting.fabric.interop.gravity;

import at.petrak.hexcasting.api.PatternRegistryBak;
import at.petrak.hexcasting.api.spell.math.HexDir;
import at.petrak.hexcasting.api.spell.math.HexPattern;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class GravityApiInterop {
    public static void init() {
        try {
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("wawawddew", HexDir.NORTH_EAST),
                modLoc("interop/gravity/get"), OpGetGravity.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("wdwdwaaqw", HexDir.NORTH_WEST),
                modLoc("interop/gravity/set"), OpChangeGravity.INSTANCE);
        } catch (PatternRegistryBak.RegisterPatternException e) {
            e.printStackTrace();
        }
    }
}
