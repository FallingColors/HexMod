package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import at.petrak.hexcasting.common.casting.ControllerInfo;
import at.petrak.hexcasting.common.lib.HexSounds;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Sent server->client when the player finishes casting a spell.
 */
public record MsgNewSpellPatternAck(ControllerInfo info, List<Component> stackDesc) {
    private static final String TAG_DESC = "desc";

    public static MsgNewSpellPatternAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        var status = ControllerInfo.Status.values()[buf.readInt()];
        var descsLen = buf.readInt();
        var desc = new ArrayList<Component>(descsLen);
        for (int i = 0; i < descsLen; i++) {
            desc.add(buf.readComponent());
        }

        return new MsgNewSpellPatternAck(
            new ControllerInfo(status),
            desc
        );
    }

    public void serialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        buf.writeInt(this.info.getStatus().ordinal());
        buf.writeInt(this.stackDesc.size());
        for (var desc : this.stackDesc) {
            buf.writeComponent(desc);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                var mc = Minecraft.getInstance();
                if (this.info.shouldQuit()) {
                    // don't pay attention to the screen, so it also stops when we die
                    mc.getSoundManager().stop(HexSounds.CASTING_AMBIANCE.getId(), null);
                }
                var screen = Minecraft.getInstance().screen;
                if (screen instanceof GuiSpellcasting spellGui) {
                    if (this.info.shouldQuit()) {
                        mc.setScreen(null);
                    } else {
                        spellGui.recvServerUpdate(this.info, this.stackDesc);
                    }
                }
            })
        );
        ctx.get().setPacketHandled(true);
    }

}
