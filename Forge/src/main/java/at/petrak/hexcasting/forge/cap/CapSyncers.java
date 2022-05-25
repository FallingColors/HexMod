package at.petrak.hexcasting.forge.cap;

import at.petrak.hexcasting.forge.network.MsgColorizerUpdateAck;
import at.petrak.hexcasting.forge.network.MsgSentinelStatusUpdateAck;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CapSyncers {
    @SubscribeEvent
    public static void copyDataOnDeath(PlayerEvent.Clone evt) {
        var eitherSidePlayer = evt.getPlayer();
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
        x.setSentinel(player, x.getSentinel(proto));
        x.setColorizer(player, x.getColorizer(proto));
        x.setHarness(player, x.getHarness(proto, InteractionHand.MAIN_HAND));
        x.setPatterns(player, x.getPatterns(proto));
    }

    @SubscribeEvent
    public static void syncDataOnLogin(PlayerEvent.PlayerLoggedInEvent evt) {
        if (!(evt.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        syncSentinel(player);
        syncColorizer(player);
    }

    @SubscribeEvent
    public static void syncDataOnRejoin(PlayerEvent.PlayerRespawnEvent evt) {
        if (!(evt.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        syncSentinel(player);
        syncColorizer(player);
    }

    public static void syncSentinel(ServerPlayer player) {
        IXplatAbstractions.INSTANCE.sendPacketToPlayer(player,
            new MsgSentinelStatusUpdateAck(IXplatAbstractions.INSTANCE.getSentinel(player)));
    }

    public static void syncColorizer(ServerPlayer player) {
        IXplatAbstractions.INSTANCE.sendPacketToPlayer(player,
            new MsgColorizerUpdateAck(IXplatAbstractions.INSTANCE.getColorizer(player)));
    }
}
