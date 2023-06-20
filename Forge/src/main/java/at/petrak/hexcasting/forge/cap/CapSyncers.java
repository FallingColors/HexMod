package at.petrak.hexcasting.forge.cap;

import at.petrak.hexcasting.forge.network.MsgAltioraUpdateAck;
import at.petrak.hexcasting.forge.network.MsgPigmentUpdateAck;
import at.petrak.hexcasting.forge.network.MsgSentinelStatusUpdateAck;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CapSyncers {
    @SubscribeEvent
    public static void copyDataOnDeath(PlayerEvent.Clone evt) {
        var eitherSidePlayer = evt.getEntity();
        // this apparently defines it in outside scope. the more you know.
        if (!(eitherSidePlayer instanceof ServerPlayer player)) {
            return;
        }

        var eitherSideProto = evt.getOriginal();
        if (!(eitherSideProto instanceof ServerPlayer proto)) {
            return;
        }

        // Copy data from this to new player
        var x = IXplatAbstractions.INSTANCE;
        x.setFlight(player, x.getFlight(proto));
        x.setAltiora(player, x.getAltiora(proto));
        x.setSentinel(player, x.getSentinel(proto));
        x.setPigment(player, x.getPigment(proto));
        x.setStaffcastImage(player, x.getStaffcastVM(proto, InteractionHand.MAIN_HAND).getImage());
        x.setPatterns(player, x.getPatternsSavedInUi(proto));
    }

    @SubscribeEvent
    public static void syncDataOnLogin(PlayerEvent.PlayerLoggedInEvent evt) {
        if (!(evt.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        syncSentinel(player);
        syncPigment(player);
        syncAltiora(player);
    }

    @SubscribeEvent
    public static void syncDataOnRejoin(PlayerEvent.PlayerRespawnEvent evt) {
        if (!(evt.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        syncSentinel(player);
        syncPigment(player);
        syncAltiora(player);
    }

    public static void syncSentinel(ServerPlayer player) {
        IXplatAbstractions.INSTANCE.sendPacketToPlayer(player,
            new MsgSentinelStatusUpdateAck(IXplatAbstractions.INSTANCE.getSentinel(player)));
    }

    public static void syncPigment(ServerPlayer player) {
        IXplatAbstractions.INSTANCE.sendPacketToPlayer(player,
            new MsgPigmentUpdateAck(IXplatAbstractions.INSTANCE.getPigment(player)));
    }

    public static void syncAltiora(ServerPlayer player) {
        IXplatAbstractions.INSTANCE.sendPacketToPlayer(player,
            new MsgAltioraUpdateAck(IXplatAbstractions.INSTANCE.getAltiora(player)));
    }
}
