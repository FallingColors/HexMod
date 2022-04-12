package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.api.spell.casting.ResolvedPattern;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Sent server->client when the player opens the spell gui to request the server provide the current stack.
 */
public record MsgOpenSpellGuiAck(InteractionHand hand, List<ResolvedPattern> patterns, List<Component> components) {

    public static MsgOpenSpellGuiAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        var hand = buf.readEnum(InteractionHand.class);

        var patternsLen = buf.readInt();
        var patterns = new ArrayList<ResolvedPattern>(patternsLen);
        for (int i = 0; i < patternsLen; i++) {
            patterns.add(ResolvedPattern.DeserializeFromNBT(buf.readAnySizeNbt()));
        }

        var descsLen = buf.readInt();
        var desc = new ArrayList<Component>(descsLen);
        for (int i = 0; i < descsLen; i++) {
            desc.add(buf.readComponent());
        }

        return new MsgOpenSpellGuiAck(hand, patterns, desc);
    }

    public void serialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        buf.writeEnum(this.hand);

        buf.writeInt(this.patterns.size());
        for (var pattern : this.patterns) {
            buf.writeNbt(pattern.serializeToNBT());
        }

        buf.writeInt(this.components.size());
        for (var desc : this.components) {
            buf.writeComponent(desc);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            GuiOpener opener = new GuiOpener(this);
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> opener::openGui);
        });
        ctx.get().setPacketHandled(true);
    }

}
