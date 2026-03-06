package at.petrak.hexcasting.forge.cap;

import at.petrak.hexcasting.forge.network.MsgAltioraUpdateAck;
import at.petrak.hexcasting.forge.network.MsgPigmentUpdateAck;
import at.petrak.hexcasting.forge.network.MsgSentinelStatusUpdateAck;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CapSyncers {
    private static final int LOGIN_SYNC_MAX_TICKS = 200;
    private static final ConcurrentHashMap<UUID, Integer> PENDING_LOGIN_SYNC = new ConcurrentHashMap<>();

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
        // PlayerLoggedInEvent fires before the connection is always ready for play packets.
        // Retry for a short window until NeoForge allows our payloads.
        PENDING_LOGIN_SYNC.put(player.getUUID(), LOGIN_SYNC_MAX_TICKS);
    }

    @SubscribeEvent
    public static void syncDataOnRejoin(PlayerEvent.PlayerRespawnEvent evt) {
        if (!(evt.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        // Respawn is in play phase; sync immediately.
        syncSentinel(player);
        syncPigment(player);
        syncAltiora(player);
    }

    @SubscribeEvent
    public static void retryLoginSync(EntityTickEvent.Post evt) {
        if (!(evt.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        var uuid = player.getUUID();
        var remaining = PENDING_LOGIN_SYNC.get(uuid);
        if (remaining == null) {
            return;
        }
        if (remaining <= 0) {
            PENDING_LOGIN_SYNC.remove(uuid);
            return;
        }

        try {
            syncSentinel(player);
            syncPigment(player);
            syncAltiora(player);
            PENDING_LOGIN_SYNC.remove(uuid);
        } catch (UnsupportedOperationException e) {
            // Still in configuration phase / channels not ready yet.
            PENDING_LOGIN_SYNC.put(uuid, remaining - 1);
        }
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
