package at.petrak.hexcasting.fabric.xplat;

import at.petrak.hexcasting.common.misc.Brainsweeping;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

public class FabricListenersSetup {
    public static void init() {
        UseEntityCallback.EVENT.register(Brainsweeping::tradeWithVillager);
    }
}
