package at.petrak.hexcasting.fabric.network;

import at.petrak.hexcasting.common.msgs.*;
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
        ServerPlayNetworking.registerGlobalReceiver(MsgNewSpellPatternC2S.ID,
            makeServerBoundHandler(MsgNewSpellPatternC2S::deserialize, MsgNewSpellPatternC2S::handle));
        ServerPlayNetworking.registerGlobalReceiver(
            MsgShiftScrollC2S.ID, makeServerBoundHandler(MsgShiftScrollC2S::deserialize, MsgShiftScrollC2S::handle));
    }

    private static <T> ServerPlayNetworking.PlayChannelHandler makeServerBoundHandler(
        Function<FriendlyByteBuf, T> decoder, TriConsumer<T, MinecraftServer, ServerPlayer> handle) {
        return (server, player, _handler, buf, _responseSender) -> handle.accept(decoder.apply(buf), server, player);
    }

    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(MsgNewSpellPatternS2C.ID,
            makeClientBoundHandler(MsgNewSpellPatternS2C::deserialize, MsgNewSpellPatternS2C::handle));
        ClientPlayNetworking.registerGlobalReceiver(
            MsgBlinkS2C.ID, makeClientBoundHandler(MsgBlinkS2C::deserialize, MsgBlinkS2C::handle));
        ClientPlayNetworking.registerGlobalReceiver(MsgCastParticleS2C.ID,
            makeClientBoundHandler(MsgCastParticleS2C::deserialize, MsgCastParticleS2C::handle));
        ClientPlayNetworking.registerGlobalReceiver(MsgOpenSpellGuiS2C.ID,
            makeClientBoundHandler(MsgOpenSpellGuiS2C::deserialize, MsgOpenSpellGuiS2C::handle));
        ClientPlayNetworking.registerGlobalReceiver(MsgBeepS2C.ID,
            makeClientBoundHandler(MsgBeepS2C::deserialize, MsgBeepS2C::handle));
        ClientPlayNetworking.registerGlobalReceiver(MsgNewWallScrollS2C.ID,
            makeClientBoundHandler(MsgNewWallScrollS2C::deserialize, MsgNewWallScrollS2C::handle));
        ClientPlayNetworking.registerGlobalReceiver(MsgRecalcWallScrollDisplayS2C.ID,
            makeClientBoundHandler(MsgRecalcWallScrollDisplayS2C::deserialize, MsgRecalcWallScrollDisplayS2C::handle));
        ClientPlayNetworking.registerGlobalReceiver(MsgNewSpiralPatternsS2C.ID,
                makeClientBoundHandler(MsgNewSpiralPatternsS2C::deserialize, MsgNewSpiralPatternsS2C::handle));
        ClientPlayNetworking.registerGlobalReceiver(MsgClearSpiralPatternsS2C.ID,
                makeClientBoundHandler(MsgClearSpiralPatternsS2C::deserialize, MsgClearSpiralPatternsS2C::handle));
    }

    private static <T> ClientPlayNetworking.PlayChannelHandler makeClientBoundHandler(
        Function<FriendlyByteBuf, T> decoder, Consumer<T> handler) {
        return (_client, _handler, buf, _responseSender) -> handler.accept(decoder.apply(buf));
    }
}
