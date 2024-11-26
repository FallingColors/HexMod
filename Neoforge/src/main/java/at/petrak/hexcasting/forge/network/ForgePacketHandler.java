package at.petrak.hexcasting.forge.network;

import at.petrak.hexcasting.common.msgs.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ForgePacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
        modLoc("main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    public static SimpleChannel getNetwork() {
        return NETWORK;
    }

    public static void init() {
        int messageIdx = 0;

        // Client -> server
        NETWORK.registerMessage(messageIdx++, MsgNewSpellPatternC2S.class, MsgNewSpellPatternC2S::serialize,
            MsgNewSpellPatternC2S::deserialize, makeServerBoundHandler(MsgNewSpellPatternC2S::handle));
        NETWORK.registerMessage(messageIdx++, MsgShiftScrollC2S.class, MsgShiftScrollC2S::serialize,
            MsgShiftScrollC2S::deserialize, makeServerBoundHandler(MsgShiftScrollC2S::handle));

        // Server -> client
        NETWORK.registerMessage(messageIdx++, MsgNewSpellPatternS2C.class, MsgNewSpellPatternS2C::serialize,
            MsgNewSpellPatternS2C::deserialize, makeClientBoundHandler(MsgNewSpellPatternS2C::handle));
        NETWORK.registerMessage(messageIdx++, MsgBlinkS2C.class, MsgBlinkS2C::serialize,
            MsgBlinkS2C::deserialize, makeClientBoundHandler(MsgBlinkS2C::handle));
        NETWORK.registerMessage(messageIdx++, MsgSentinelStatusUpdateAck.class, MsgSentinelStatusUpdateAck::serialize,
            MsgSentinelStatusUpdateAck::deserialize, makeClientBoundHandler(MsgSentinelStatusUpdateAck::handle));
        NETWORK.registerMessage(messageIdx++, MsgPigmentUpdateAck.class, MsgPigmentUpdateAck::serialize,
            MsgPigmentUpdateAck::deserialize, makeClientBoundHandler(MsgPigmentUpdateAck::handle));
        NETWORK.registerMessage(messageIdx++, MsgAltioraUpdateAck.class, MsgAltioraUpdateAck::serialize,
            MsgAltioraUpdateAck::deserialize, makeClientBoundHandler(MsgAltioraUpdateAck::handle));
        NETWORK.registerMessage(messageIdx++, MsgCastParticleS2C.class, MsgCastParticleS2C::serialize,
            MsgCastParticleS2C::deserialize, makeClientBoundHandler(MsgCastParticleS2C::handle));
        NETWORK.registerMessage(messageIdx++, MsgOpenSpellGuiS2C.class, MsgOpenSpellGuiS2C::serialize,
            MsgOpenSpellGuiS2C::deserialize, makeClientBoundHandler(MsgOpenSpellGuiS2C::handle));
        NETWORK.registerMessage(messageIdx++, MsgBeepS2C.class, MsgBeepS2C::serialize,
            MsgBeepS2C::deserialize, makeClientBoundHandler(MsgBeepS2C::handle));
        NETWORK.registerMessage(messageIdx++, MsgBrainsweepAck.class, MsgBrainsweepAck::serialize,
            MsgBrainsweepAck::deserialize, makeClientBoundHandler(MsgBrainsweepAck::handle));
        NETWORK.registerMessage(messageIdx++, MsgNewWallScrollS2C.class, MsgNewWallScrollS2C::serialize,
            MsgNewWallScrollS2C::deserialize, makeClientBoundHandler(MsgNewWallScrollS2C::handle));
        NETWORK.registerMessage(messageIdx++, MsgRecalcWallScrollDisplayS2C.class, MsgRecalcWallScrollDisplayS2C::serialize,
            MsgRecalcWallScrollDisplayS2C::deserialize, makeClientBoundHandler(MsgRecalcWallScrollDisplayS2C::handle));
        NETWORK.registerMessage(messageIdx++, MsgNewSpiralPatternsS2C.class, MsgNewSpiralPatternsS2C::serialize,
                MsgNewSpiralPatternsS2C::deserialize, makeClientBoundHandler(MsgNewSpiralPatternsS2C::handle));
        NETWORK.registerMessage(messageIdx++, MsgClearSpiralPatternsS2C.class, MsgClearSpiralPatternsS2C::serialize,
                MsgClearSpiralPatternsS2C::deserialize, makeClientBoundHandler(MsgClearSpiralPatternsS2C::handle));
    }

    private static <T> BiConsumer<T, Supplier<NetworkEvent.Context>> makeServerBoundHandler(
        TriConsumer<T, MinecraftServer, ServerPlayer> handler) {
        return (m, ctx) -> {
            handler.accept(m, ctx.get().getSender().getServer(), ctx.get().getSender());
            ctx.get().setPacketHandled(true);
        };
    }

    private static <T> BiConsumer<T, Supplier<NetworkEvent.Context>> makeClientBoundHandler(Consumer<T> consumer) {
        return (m, ctx) -> {
            consumer.accept(m);
            ctx.get().setPacketHandled(true);
        };
    }
}
