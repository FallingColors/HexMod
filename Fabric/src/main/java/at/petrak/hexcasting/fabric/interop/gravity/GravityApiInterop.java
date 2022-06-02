package at.petrak.hexcasting.fabric.interop.gravity;

import at.petrak.hexcasting.api.PatternRegistry;
import at.petrak.hexcasting.api.spell.math.HexDir;
import at.petrak.hexcasting.api.spell.math.HexPattern;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class GravityApiInterop {
    public static void init() {
        try {
            PatternRegistry.mapPattern(HexPattern.fromAngles("wawawddew", HexDir.NORTH_EAST),
                modLoc("interop/gravity/get"), OpGetGravity.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("wdwdwaaqw", HexDir.NORTH_WEST),
                modLoc("interop/gravity/set"), OpChangeGravity.INSTANCE);
        } catch (PatternRegistry.RegisterPatternException e) {
            e.printStackTrace();
        }
    }
}
