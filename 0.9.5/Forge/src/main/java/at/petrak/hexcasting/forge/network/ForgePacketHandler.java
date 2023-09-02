package at.petrak.hexcasting.forge.network;

import at.petrak.hexcasting.common.network.*;
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
        NETWORK.registerMessage(messageIdx++, MsgNewSpellPatternSyn.class, MsgNewSpellPatternSyn::serialize,
            MsgNewSpellPatternSyn::deserialize, makeServerBoundHandler(MsgNewSpellPatternSyn::handle));
        NETWORK.registerMessage(messageIdx++, MsgShiftScrollSyn.class, MsgShiftScrollSyn::serialize,
            MsgShiftScrollSyn::deserialize, makeServerBoundHandler(MsgShiftScrollSyn::handle));

        // Server -> client
        NETWORK.registerMessage(messageIdx++, MsgNewSpellPatternAck.class, MsgNewSpellPatternAck::serialize,
            MsgNewSpellPatternAck::deserialize, makeClientBoundHandler(MsgNewSpellPatternAck::handle));
        NETWORK.registerMessage(messageIdx++, MsgBlinkAck.class, MsgBlinkAck::serialize,
            MsgBlinkAck::deserialize, makeClientBoundHandler(MsgBlinkAck::handle));
        NETWORK.registerMessage(messageIdx++, MsgSentinelStatusUpdateAck.class, MsgSentinelStatusUpdateAck::serialize,
            MsgSentinelStatusUpdateAck::deserialize, makeClientBoundHandler(MsgSentinelStatusUpdateAck::handle));
        NETWORK.registerMessage(messageIdx++, MsgColorizerUpdateAck.class, MsgColorizerUpdateAck::serialize,
            MsgColorizerUpdateAck::deserialize, makeClientBoundHandler(MsgColorizerUpdateAck::handle));
        NETWORK.registerMessage(messageIdx++, MsgCastParticleAck.class, MsgCastParticleAck::serialize,
            MsgCastParticleAck::deserialize, makeClientBoundHandler(MsgCastParticleAck::handle));
        NETWORK.registerMessage(messageIdx++, MsgOpenSpellGuiAck.class, MsgOpenSpellGuiAck::serialize,
            MsgOpenSpellGuiAck::deserialize, makeClientBoundHandler(MsgOpenSpellGuiAck::handle));
        NETWORK.registerMessage(messageIdx++, MsgBeepAck.class, MsgBeepAck::serialize,
            MsgBeepAck::deserialize, makeClientBoundHandler(MsgBeepAck::handle));
        NETWORK.registerMessage(messageIdx++, MsgBrainsweepAck.class, MsgBrainsweepAck::serialize,
            MsgBrainsweepAck::deserialize, makeClientBoundHandler(MsgBrainsweepAck::handle));
        NETWORK.registerMessage(messageIdx++, MsgUpdateComparatorVisualsAck.class,
            MsgUpdateComparatorVisualsAck::serialize,
            MsgUpdateComparatorVisualsAck::deserialize,
            makeClientBoundHandler(MsgUpdateComparatorVisualsAck::handle));
        NETWORK.registerMessage(messageIdx++, MsgNewWallScrollAck.class,
            MsgNewWallScrollAck::serialize,
            MsgNewWallScrollAck::deserialize,
            makeClientBoundHandler(MsgNewWallScrollAck::handle));
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
