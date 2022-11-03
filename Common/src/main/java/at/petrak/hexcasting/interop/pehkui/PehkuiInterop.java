package at.petrak.hexcasting.interop.pehkui;

import at.petrak.hexcasting.api.PatternRegistry;
import at.petrak.hexcasting.api.spell.math.HexDir;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import net.minecraft.world.entity.Entity;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class PehkuiInterop {
    public static void init() {
        try {
            PatternRegistry.mapPattern(HexPattern.fromAngles("aawawwawwa", HexDir.NORTH_WEST),
                modLoc("interop/pehkui/get"), OpGetScale.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("ddwdwwdwwd", HexDir.NORTH_EAST),
                modLoc("interop/pehkui/set"), OpSetScale.INSTANCE);
        } catch (PatternRegistry.RegisterPatternException e) {
            e.printStackTrace();
        }
    }

    /**
     * Pehkui doesn't publish an API jar so we do this BS
     */
    public interface ApiAbstraction {
        float getScale(Entity e);

        void setScale(Entity e, float scale);
    }
}
