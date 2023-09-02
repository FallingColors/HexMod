package at.petrak.hexcasting.fabric.network;

import at.petrak.hexcasting.common.network.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.Consumer;
import java.util.function.Function;

public class FabricPacketHandler {
    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(MsgNewSpellPatternSyn.ID,
            makeServerBoundHandler(MsgNewSpellPatternSyn::deserialize, MsgNewSpellPatternSyn::handle));
        ServerPlayNetworking.registerGlobalReceiver(
            MsgShiftScrollSyn.ID, makeServerBoundHandler(MsgShiftScrollSyn::deserialize, MsgShiftScrollSyn::handle));
    }

    private static <T> ServerPlayNetworking.PlayChannelHandler makeServerBoundHandler(
        Function<FriendlyByteBuf, T> decoder, TriConsumer<T, MinecraftServer, ServerPlayer> handle) {
        return (server, player, _handler, buf, _responseSender) -> handle.accept(decoder.apply(buf), server, player);
    }

    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(MsgNewSpellPatternAck.ID,
            makeClientBoundHandler(MsgNewSpellPatternAck::deserialize, MsgNewSpellPatternAck::handle));
        ClientPlayNetworking.registerGlobalReceiver(
            MsgBlinkAck.ID, makeClientBoundHandler(MsgBlinkAck::deserialize, MsgBlinkAck::handle));
        ClientPlayNetworking.registerGlobalReceiver(MsgCastParticleAck.ID,
            makeClientBoundHandler(MsgCastParticleAck::deserialize, MsgCastParticleAck::handle));
        ClientPlayNetworking.registerGlobalReceiver(MsgOpenSpellGuiAck.ID,
            makeClientBoundHandler(MsgOpenSpellGuiAck::deserialize, MsgOpenSpellGuiAck::handle));
        ClientPlayNetworking.registerGlobalReceiver(MsgBeepAck.ID,
            makeClientBoundHandler(MsgBeepAck::deserialize, MsgBeepAck::handle));
        ClientPlayNetworking.registerGlobalReceiver(MsgUpdateComparatorVisualsAck.ID,
            makeClientBoundHandler(MsgUpdateComparatorVisualsAck::deserialize, MsgUpdateComparatorVisualsAck::handle));
        ClientPlayNetworking.registerGlobalReceiver(MsgNewWallScrollAck.ID,
            makeClientBoundHandler(MsgNewWallScrollAck::deserialize, MsgNewWallScrollAck::handle));
    }

    private static <T> ClientPlayNetworking.PlayChannelHandler makeClientBoundHandler(
        Function<FriendlyByteBuf, T> decoder, Consumer<T> handler) {
        return (_client, _handler, buf, _responseSender) -> handler.accept(decoder.apply(buf));
    }
}
