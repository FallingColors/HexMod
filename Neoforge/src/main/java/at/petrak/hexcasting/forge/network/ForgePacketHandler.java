package at.petrak.hexcasting.forge.network;

import at.petrak.hexcasting.common.msgs.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.Consumer;

public class ForgePacketHandler {

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
