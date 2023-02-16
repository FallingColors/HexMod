package at.petrak.hexcasting.fabric.interop.gravity;

import at.petrak.hexcasting.interop.HexInterop;
import at.petrak.hexcasting.xplat.IXplatAbstractions;

public class GravityApiInterop {
    public static void init() {
    }

    public static boolean isActive() {
        return IXplatAbstractions.INSTANCE.isModPresent(HexInterop.Fabric.GRAVITY_CHANGER_API_ID);
    }
}
