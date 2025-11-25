package at.petrak.hexcasting.forge.network;

import at.petrak.hexcasting.common.msgs.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handlers.ClientPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ForgePacketHandler {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        // Client -> server
        registrar.playToServer(MsgNewSpellPatternC2S.ID, MsgNewSpellPatternC2S.CODEC, makeServerBoundHandler(MsgNewSpellPatternC2S::handle));
        registrar.playToServer(MsgShiftScrollC2S.ID, MsgShiftScrollC2S.CODEC, makeServerBoundHandler(MsgShiftScrollC2S::handle));

        // Server -> client
        registrar.playToClient(MsgNewSpellPatternS2C.ID, MsgNewSpellPatternS2C.CODEC, makeClientBoundHandler(MsgNewSpellPatternS2C::handle));
        registrar.playToClient(MsgNewSpellPatternS2C.ID,      MsgNewSpellPatternS2C.CODEC,      makeClientBoundHandler(MsgNewSpellPatternS2C::handle));
        registrar.playToClient(MsgBlinkS2C.ID,                MsgBlinkS2C.CODEC,                makeClientBoundHandler(MsgBlinkS2C::handle));
        registrar.playToClient(MsgSentinelStatusUpdateAck.ID, MsgSentinelStatusUpdateAck.CODEC, makeClientBoundHandler(MsgSentinelStatusUpdateAck::handle));
        registrar.playToClient(MsgPigmentUpdateAck.ID,        MsgPigmentUpdateAck.CODEC,        makeClientBoundHandler(MsgPigmentUpdateAck::handle));
        registrar.playToClient(MsgAltioraUpdateAck.ID,        MsgAltioraUpdateAck.CODEC,        makeClientBoundHandler(MsgAltioraUpdateAck::handle));
        registrar.playToClient(MsgCastParticleS2C.ID,         MsgCastParticleS2C.CODEC,         makeClientBoundHandler(MsgCastParticleS2C::handle));
        registrar.playToClient(MsgOpenSpellGuiS2C.ID,         MsgOpenSpellGuiS2C.CODEC,         makeClientBoundHandler(MsgOpenSpellGuiS2C::handle));
        registrar.playToClient(MsgBeepS2C.ID,                 MsgBeepS2C.CODEC,                 makeClientBoundHandler(MsgBeepS2C::handle));
        registrar.playToClient(MsgBrainsweepAck.ID,           MsgBrainsweepAck.CODEC,           makeClientBoundHandler(MsgBrainsweepAck::handle));
        registrar.playToClient(MsgNewWallScrollS2C.ID,        MsgNewWallScrollS2C.CODEC,        makeClientBoundHandler(MsgNewWallScrollS2C::handle));
        registrar.playToClient(MsgRecalcWallScrollDisplayS2C.ID, MsgRecalcWallScrollDisplayS2C.CODEC, makeClientBoundHandler(MsgRecalcWallScrollDisplayS2C::handle));
        registrar.playToClient(MsgNewSpiralPatternsS2C.ID,    MsgNewSpiralPatternsS2C.CODEC,    makeClientBoundHandler(MsgNewSpiralPatternsS2C::handle));
        registrar.playToClient(MsgClearSpiralPatternsS2C.ID,  MsgClearSpiralPatternsS2C.CODEC,  makeClientBoundHandler(MsgClearSpiralPatternsS2C::handle));
    }

    private static <T extends CustomPacketPayload> IPayloadHandler<T> makeServerBoundHandler(
        TriConsumer<T, MinecraftServer, ServerPlayer> handler) {
        return (m, ctx) -> {
            handler.accept(m, ctx.player().getServer(), (ServerPlayer) ctx.player());
        };
    }

    private static <T extends CustomPacketPayload> IPayloadHandler<T> makeClientBoundHandler(Consumer<T> consumer) {
        return (m, ctx) -> {
            consumer.accept(m);
        };
    }
}
