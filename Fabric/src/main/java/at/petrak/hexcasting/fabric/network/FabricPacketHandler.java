package at.petrak.hexcasting.fabric.network;

import at.petrak.hexcasting.common.msgs.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.Consumer;
import java.util.function.Function;

public class FabricPacketHandler {
    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(MsgNewSpellPatternC2S.TYPE,
            makeServerBoundHandler(MsgNewSpellPatternC2S::handle));
        ServerPlayNetworking.registerGlobalReceiver(
            MsgShiftScrollC2S.TYPE, makeServerBoundHandler(MsgShiftScrollC2S::handle));
    }

    private static <T extends CustomPacketPayload> ServerPlayNetworking.PlayPayloadHandler<T> makeServerBoundHandler(
            TriConsumer<T, MinecraftServer, ServerPlayer> handle) {
        return (payload, context) -> handle.accept(payload, context.server(), context.player());
    }

    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(MsgNewSpellPatternS2C.TYPE,
            makeClientBoundHandler(MsgNewSpellPatternS2C::handle));
        ClientPlayNetworking.registerGlobalReceiver(
            MsgBlinkS2C.TYPE, makeClientBoundHandler(MsgBlinkS2C::handle));
        ClientPlayNetworking.registerGlobalReceiver(MsgCastParticleS2C.TYPE,
            makeClientBoundHandler(MsgCastParticleS2C::handle));
        ClientPlayNetworking.registerGlobalReceiver(MsgOpenSpellGuiS2C.TYPE,
            makeClientBoundHandler(MsgOpenSpellGuiS2C::handle));
        ClientPlayNetworking.registerGlobalReceiver(MsgBeepS2C.TYPE,
            makeClientBoundHandler(MsgBeepS2C::handle));
        ClientPlayNetworking.registerGlobalReceiver(MsgNewWallScrollS2C.TYPE,
            makeClientBoundHandler(MsgNewWallScrollS2C::handle));
        ClientPlayNetworking.registerGlobalReceiver(MsgRecalcWallScrollDisplayS2C.TYPE,
            makeClientBoundHandler(MsgRecalcWallScrollDisplayS2C::handle));
        ClientPlayNetworking.registerGlobalReceiver(MsgNewSpiralPatternsS2C.TYPE,
                makeClientBoundHandler(MsgNewSpiralPatternsS2C::handle));
        ClientPlayNetworking.registerGlobalReceiver(MsgClearSpiralPatternsS2C.TYPE,
                makeClientBoundHandler(MsgClearSpiralPatternsS2C::handle));
    }

    private static <T extends CustomPacketPayload> ClientPlayNetworking.PlayPayloadHandler<T> makeClientBoundHandler(Consumer<T> handler) {
        return (payload, context) -> handler.accept(payload);
    }
}
