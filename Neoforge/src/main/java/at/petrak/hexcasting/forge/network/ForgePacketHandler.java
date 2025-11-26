package at.petrak.hexcasting.forge.network;

import at.petrak.hexcasting.common.msgs.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
<<<<<<< HEAD
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handlers.ClientPayloadHandler;
=======
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
>>>>>>> refs/remotes/slava/devel/port-1.21
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.Consumer;

public class ForgePacketHandler {

<<<<<<< HEAD
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
=======
    public static void init(IEventBus modBus) {
        modBus.addListener(RegisterPayloadHandlersEvent.class, ev -> {
            final PayloadRegistrar registar = ev.registrar("1");

            // Client -> server
            registar.playToServer(MsgNewSpellPatternC2S.TYPE, MsgNewSpellPatternC2S.STREAM_CODEC,
                    makeServerBoundHandler(MsgNewSpellPatternC2S::handle));
            registar.playToServer(MsgShiftScrollC2S.TYPE, MsgShiftScrollC2S.STREAM_CODEC,
                    makeServerBoundHandler(MsgShiftScrollC2S::handle));

            // Server -> client
            registar.playToClient(MsgNewSpellPatternS2C.TYPE, MsgNewSpellPatternS2C.STREAM_CODEC,
                    makeClientBoundHandler(MsgNewSpellPatternS2C::handle));
            registar.playToClient(MsgBlinkS2C.TYPE, MsgBlinkS2C.STREAM_CODEC,
                    makeClientBoundHandler(MsgBlinkS2C::handle));
            registar.playToClient(MsgSentinelStatusUpdateAck.TYPE, MsgSentinelStatusUpdateAck.STREAM_CODEC,
                    makeClientBoundHandler(MsgSentinelStatusUpdateAck::handle));
            registar.playToClient(MsgPigmentUpdateAck.TYPE, MsgPigmentUpdateAck.STREAM_CODEC,
                    makeClientBoundHandler(MsgPigmentUpdateAck::handle));
            registar.playToClient(MsgAltioraUpdateAck.TYPE, MsgAltioraUpdateAck.STREAM_CODEC,
                    makeClientBoundHandler(MsgAltioraUpdateAck::handle));
            registar.playToClient(MsgCastParticleS2C.TYPE, MsgCastParticleS2C.STREAM_CODEC,
                    makeClientBoundHandler(MsgCastParticleS2C::handle));
            registar.playToClient(MsgOpenSpellGuiS2C.TYPE, MsgOpenSpellGuiS2C.STREAM_CODEC,
                    makeClientBoundHandler(MsgOpenSpellGuiS2C::handle));
            registar.playToClient(MsgBeepS2C.TYPE, MsgBeepS2C.STREAM_CODEC,
                    makeClientBoundHandler(MsgBeepS2C::handle));
            registar.playToClient(MsgBrainsweepAck.TYPE, MsgBrainsweepAck.STREAM_CODEC,
                    makeClientBoundHandler(MsgBrainsweepAck::handle));
            registar.playToClient(MsgNewWallScrollS2C.TYPE, MsgNewWallScrollS2C.STREAM_CODEC,
                    makeClientBoundHandler(MsgNewWallScrollS2C::handle));
            registar.playToClient(MsgRecalcWallScrollDisplayS2C.TYPE, MsgRecalcWallScrollDisplayS2C.STREAM_CODEC,
                    makeClientBoundHandler(MsgRecalcWallScrollDisplayS2C::handle));
            registar.playToClient(MsgNewSpiralPatternsS2C.TYPE, MsgNewSpiralPatternsS2C.STREAM_CODEC,
                    makeClientBoundHandler(MsgNewSpiralPatternsS2C::handle));
            registar.playToClient(MsgClearSpiralPatternsS2C.TYPE, MsgClearSpiralPatternsS2C.STREAM_CODEC,
                    makeClientBoundHandler(MsgClearSpiralPatternsS2C::handle));
        });
>>>>>>> refs/remotes/slava/devel/port-1.21
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
