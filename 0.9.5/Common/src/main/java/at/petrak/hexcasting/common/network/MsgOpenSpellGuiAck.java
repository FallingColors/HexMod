package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.api.spell.casting.ResolvedPattern;
import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

import java.util.ArrayList;
import java.util.List;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent server->client when the player opens the spell gui to request the server provide the current stack.
 */
public record MsgOpenSpellGuiAck(InteractionHand hand, List<ResolvedPattern> patterns, List<Component> components)
    implements IMessage {
    public static final ResourceLocation ID = modLoc("cgui");

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgOpenSpellGuiAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        var hand = buf.readEnum(InteractionHand.class);

        var patternsLen = buf.readInt();
        var patterns = new ArrayList<ResolvedPattern>(patternsLen);
        for (int i = 0; i < patternsLen; i++) {
            patterns.add(ResolvedPattern.fromNBT(buf.readAnySizeNbt()));
        }

        var descsLen = buf.readInt();
        var desc = new ArrayList<Component>(descsLen);
        for (int i = 0; i < descsLen; i++) {
            desc.add(buf.readComponent());
        }

        return new MsgOpenSpellGuiAck(hand, patterns, desc);
    }

    public void serialize(FriendlyByteBuf buf) {
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

    public static void handle(MsgOpenSpellGuiAck msg) {
        Minecraft.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                var mc = Minecraft.getInstance();
                mc.setScreen(new GuiSpellcasting(msg.hand(), msg.patterns(), msg.components()));
            }
        });
    }
}
