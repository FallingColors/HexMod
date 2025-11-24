package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.api.casting.eval.ResolvedPattern;
import at.petrak.hexcasting.api.casting.eval.env.StaffCastEnv;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

import java.util.ArrayList;
import java.util.List;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent client->server when the player finishes drawing a pattern.
 * Server will send back a {@link MsgNewSpellPatternS2C} packet
 */
public record MsgNewSpellPatternC2S(InteractionHand handUsed, HexPattern pattern,
                                    List<ResolvedPattern> resolvedPatterns)
    implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, MsgNewSpellPatternC2S> CODEC = CustomPacketPayload.codec(MsgNewSpellPatternC2S::serialize, MsgNewSpellPatternC2S::deserialize);
    public static final Type<MsgNewSpellPatternC2S> ID = new Type<>(modLoc("pat_cs"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static MsgNewSpellPatternC2S deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        var hand = buf.readEnum(InteractionHand.class);
        var pattern = HexPattern.fromNBT(buf.readNbt());

        var resolvedPatternsLen = buf.readInt();
        var resolvedPatterns = new ArrayList<ResolvedPattern>(resolvedPatternsLen);
        for (int i = 0; i < resolvedPatternsLen; i++) {
            resolvedPatterns.add(ResolvedPattern.fromNBT(buf.readNbt()));
        }
        return new MsgNewSpellPatternC2S(hand, pattern, resolvedPatterns);
    }

    public void serialize(FriendlyByteBuf buf) {
        buf.writeEnum(handUsed);
        buf.writeNbt(this.pattern.serializeToNBT());
        buf.writeInt(this.resolvedPatterns.size());
        for (var pat : this.resolvedPatterns) {
            buf.writeNbt(pat.serializeToNBT());
        }
    }

    public void handle(MinecraftServer server, ServerPlayer sender) {
        server.execute(() -> StaffCastEnv.handleNewPatternOnServer(sender, this));
    }
}
