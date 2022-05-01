package at.petrak.hexcasting.forge;

import at.petrak.hexcasting.forge.network.ForgePacketHandler;
import at.petrak.hexcasting.forge.network.MsgBrainsweepAck;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

public class ForgeOnlyEvents {
    // On Fabric this should be auto-synced
    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking evt) {
        Entity target = evt.getTarget();
        if (evt.getPlayer() instanceof ServerPlayer serverPlayer &&
            target instanceof Mob mob && IXplatAbstractions.INSTANCE.isBrainswept(mob)) {
            ForgePacketHandler.getNetwork()
                .send(PacketDistributor.PLAYER.with(() -> serverPlayer), MsgBrainsweepAck.of(mob));
        }
    }
}
