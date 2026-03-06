package at.petrak.hexcasting.forge.network;

import at.petrak.hexcasting.common.msgs.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import static at.petrak.hexcasting.api.HexAPI.MOD_ID;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = MOD_ID)
public class ForgePacketHandler {

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        final var registrar = event.registrar("1");

        // Client -> server
        registrar.playToServer(
            MsgNewSpellPatternC2S.TYPE,
            MsgNewSpellPatternC2S.STREAM_CODEC,
            (payload, context) -> payload.handle(
                context.player().getServer(),
                (net.minecraft.server.level.ServerPlayer) context.player()
            )
        );
        registrar.playToServer(
            MsgShiftScrollC2S.TYPE,
            MsgShiftScrollC2S.STREAM_CODEC,
            (payload, context) -> payload.handle(
                context.player().getServer(),
                (net.minecraft.server.level.ServerPlayer) context.player()
            )
        );

        // Server -> client: playToClient with mixin bypass for checkPacket (commonToClient registers
        // for CONFIGURATION phase only, causing encoder ClassCastException during PLAY).
        registrar.playToClient(MsgNewSpellPatternS2C.TYPE, MsgNewSpellPatternS2C.STREAM_CODEC, (payload, ctx) -> MsgNewSpellPatternS2C.handle(payload));
        registrar.playToClient(MsgSentinelStatusUpdateAck.TYPE, MsgSentinelStatusUpdateAck.STREAM_CODEC, (payload, ctx) -> MsgSentinelStatusUpdateAck.handle(payload));
        registrar.playToClient(MsgPigmentUpdateAck.TYPE, MsgPigmentUpdateAck.STREAM_CODEC, (payload, ctx) -> MsgPigmentUpdateAck.handle(payload));
        registrar.playToClient(MsgAltioraUpdateAck.TYPE, MsgAltioraUpdateAck.STREAM_CODEC, (payload, ctx) -> MsgAltioraUpdateAck.handle(payload));
        registrar.playToClient(MsgCastParticleS2C.TYPE, MsgCastParticleS2C.STREAM_CODEC, (payload, ctx) -> MsgCastParticleS2C.handle(payload));
        registrar.playToClient(MsgOpenSpellGuiS2C.TYPE, MsgOpenSpellGuiS2C.STREAM_CODEC, (payload, ctx) -> MsgOpenSpellGuiS2C.handle(payload));
        registrar.playToClient(MsgBeepS2C.TYPE, MsgBeepS2C.STREAM_CODEC, (payload, ctx) -> MsgBeepS2C.handle(payload));
        registrar.playToClient(MsgBrainsweepAck.TYPE, MsgBrainsweepAck.STREAM_CODEC, (payload, ctx) -> MsgBrainsweepAck.handle(payload));
        registrar.playToClient(MsgNewWallScrollS2C.TYPE, MsgNewWallScrollS2C.STREAM_CODEC, (payload, ctx) -> MsgNewWallScrollS2C.handle(payload));
        registrar.playToClient(MsgRecalcWallScrollDisplayS2C.TYPE, MsgRecalcWallScrollDisplayS2C.STREAM_CODEC, (payload, ctx) -> MsgRecalcWallScrollDisplayS2C.handle(payload));
        registrar.playToClient(MsgNewSpiralPatternsS2C.TYPE, MsgNewSpiralPatternsS2C.STREAM_CODEC, (payload, ctx) -> MsgNewSpiralPatternsS2C.handle(payload));
        registrar.playToClient(MsgClearSpiralPatternsS2C.TYPE, MsgClearSpiralPatternsS2C.STREAM_CODEC, (payload, ctx) -> MsgClearSpiralPatternsS2C.handle(payload));
    }
}
