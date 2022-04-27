package at.petrak.hexcasting.forge.xplat;

import at.petrak.hexcasting.common.misc.Brainsweeping;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class ForgeListenersSetup {
    public static void init() {
        var evBus = MinecraftForge.EVENT_BUS;

        evBus.addListener((PlayerInteractEvent.EntityInteract evt) -> {
            var res = Brainsweeping.tradeWithVillager(evt.getPlayer(), evt.getWorld(), evt.getHand(), evt.getTarget(),
                null);
            if (res.consumesAction()) {
                evt.setCanceled(true);
                evt.setCancellationResult(res);
            }
        });
    }
}
