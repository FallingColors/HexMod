package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.eval.ResolvedPattern;
import at.petrak.hexcasting.api.casting.eval.env.StaffCastEnv;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

import java.util.ArrayList;
import java.util.List;

/**
 * Sent client->server when the player finishes drawing a pattern.
 * Server will send back a {@link MsgNewSpellPatternS2C} packet
 */
public record MsgNewSpellPatternC2S(InteractionHand handUsed, HexPattern pattern,
                                    List<ResolvedPattern> resolvedPatterns) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MsgNewSpellPatternC2S> TYPE = new CustomPacketPayload.Type<>(HexAPI.modLoc("pat_cs"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgNewSpellPatternC2S> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL.map(
                    isMain -> isMain ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                    hand -> hand == InteractionHand.MAIN_HAND
            ), MsgNewSpellPatternC2S::handUsed,
            HexPattern.STREAM_CODEC, MsgNewSpellPatternC2S::pattern,
            ResolvedPattern.STREAM_CODEC.apply(ByteBufCodecs.list()), MsgNewSpellPatternC2S::resolvedPatterns,
            MsgNewSpellPatternC2S::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(MinecraftServer server, ServerPlayer sender) {
        server.execute(() -> StaffCastEnv.handleNewPatternOnServer(sender, this));
    }
}
