package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.api.spell.casting.ResolvedPattern;
import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

import java.util.List;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent server->client when the player opens the spell gui to request the server provide the current stack.
 */
public record MsgOpenSpellGuiAck(InteractionHand hand, List<ResolvedPattern> patterns,
                                 List<CompoundTag> stack,
                                 List<CompoundTag> parenthesized,
                                 CompoundTag ravenmind,
                                 int parenCount
)
    implements IMessage {
    public static final ResourceLocation ID = modLoc("cgui");

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgOpenSpellGuiAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        var hand = buf.readEnum(InteractionHand.class);

        var patterns = buf.readList(fbb -> ResolvedPattern.fromNBT(fbb.readAnySizeNbt()));

        var stack = buf.readList(FriendlyByteBuf::readNbt);
        var parens = buf.readList(FriendlyByteBuf::readNbt);
        var raven = buf.readAnySizeNbt();

        var parenCount = buf.readVarInt();

        return new MsgOpenSpellGuiAck(hand, patterns, stack, parens, raven, parenCount);
    }

    public void serialize(FriendlyByteBuf buf) {
        buf.writeEnum(this.hand);

        buf.writeCollection(this.patterns, (fbb, pat) -> fbb.writeNbt(pat.serializeToNBT()));

        buf.writeCollection(this.stack, FriendlyByteBuf::writeNbt);
        buf.writeCollection(this.parenthesized, FriendlyByteBuf::writeNbt);
        buf.writeNbt(this.ravenmind);

        buf.writeVarInt(this.parenCount);
    }

    public static void handle(MsgOpenSpellGuiAck msg) {
        Minecraft.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                var mc = Minecraft.getInstance();
                mc.setScreen(
                    new GuiSpellcasting(msg.hand(), msg.patterns(), msg.stack, msg.parenthesized, msg.ravenmind,
                        msg.parenCount));
            }
        });
    }
}
