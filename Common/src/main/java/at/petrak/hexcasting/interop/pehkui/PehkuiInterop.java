package at.petrak.hexcasting.interop.pehkui;

import at.petrak.hexcasting.interop.HexInterop;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.world.entity.Entity;

public class PehkuiInterop {
    public static void init() {
        // for future work
    }

    public static boolean isActive() {
        return IXplatAbstractions.INSTANCE.isModPresent(HexInterop.PEHKUI_ID);
    }

    /**
     * Pehkui doesn't publish an API jar so we do this BS
     */
    public interface ApiAbstraction {
        float getScale(Entity e);

        void setScale(Entity e, float scale);
    }
}
