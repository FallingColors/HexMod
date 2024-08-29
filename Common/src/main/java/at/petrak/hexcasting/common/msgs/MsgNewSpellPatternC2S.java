package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.api.casting.eval.ResolvedPattern;
import at.petrak.hexcasting.api.casting.eval.env.StaffCastEnv;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.utils.HexUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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
    implements IMessage {
    public static final ResourceLocation ID = modLoc("pat_cs");

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgNewSpellPatternC2S deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        var hand = buf.readEnum(InteractionHand.class);
        var pattern = HexUtils.deserializeWithCodec(buf.readNbt(), HexPattern.CODEC);

        var resolvedPatternsLen = buf.readInt();
        var resolvedPatterns = new ArrayList<ResolvedPattern>(resolvedPatternsLen);
        for (int i = 0; i < resolvedPatternsLen; i++) {
            resolvedPatterns.add(HexUtils.deserializeWithCodec(buf.readNbt(), ResolvedPattern.CODEC));
        }
        return new MsgNewSpellPatternC2S(hand, pattern, resolvedPatterns);
    }

    @Override
    public void serialize(FriendlyByteBuf buf) {
        buf.writeEnum(handUsed);
        buf.writeNbt((CompoundTag) HexUtils.serializeWithCodec(this.pattern, HexPattern.CODEC));
        buf.writeInt(this.resolvedPatterns.size());
        for (var pat : this.resolvedPatterns) {
            buf.writeNbt((CompoundTag) HexUtils.serializeWithCodec(pat, ResolvedPattern.CODEC));
        }
    }

    public void handle(MinecraftServer server, ServerPlayer sender) {
        server.execute(() -> StaffCastEnv.handleNewPatternOnServer(sender, this));
    }
}
