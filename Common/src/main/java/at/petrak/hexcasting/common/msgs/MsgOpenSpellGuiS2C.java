package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.api.casting.eval.ResolvedPattern;
import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

import java.util.List;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent server->client when the player opens the spell gui to request the server provide the current stack.
 */
public record MsgOpenSpellGuiS2C(InteractionHand hand, List<ResolvedPattern> patterns,
                                 List<CompoundTag> stack,
                                 CompoundTag ravenmind,
                                 int parenCount
)
    implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, MsgOpenSpellGuiS2C> CODEC = CustomPacketPayload.codec(MsgOpenSpellGuiS2C::serialize, MsgOpenSpellGuiS2C::deserialize);
    public static final Type<MsgOpenSpellGuiS2C> ID = new Type<>(modLoc("cgui"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static MsgOpenSpellGuiS2C deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        var hand = buf.readEnum(InteractionHand.class);

        var patterns = buf.readList(fbb -> ResolvedPattern.fromNBT(fbb.readNbt()));

        var stack = buf.readList(b -> b.readNbt());
        var raven = buf.readNbt();

        var parenCount = buf.readVarInt();

        return new MsgOpenSpellGuiS2C(hand, patterns, stack, raven, parenCount);
    }

    public void serialize(FriendlyByteBuf buf) {
        buf.writeEnum(this.hand);

        buf.writeCollection(this.patterns, (fbb, pat) -> fbb.writeNbt(pat.serializeToNBT()));

        buf.writeCollection(this.stack, (b, t) -> b.writeNbt(t));
        buf.writeNbt(this.ravenmind);

        buf.writeVarInt(this.parenCount);
    }

    public static void handle(MsgOpenSpellGuiS2C msg) {
        Minecraft.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                var mc = Minecraft.getInstance();
                mc.setScreen(
                    new GuiSpellcasting(msg.hand(), msg.patterns(), msg.stack, msg.ravenmind,
                        msg.parenCount));
            }
        });
    }
}
