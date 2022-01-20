package at.petrak.hex.common.network;

import at.petrak.hex.client.gui.GuiSpellcasting;
import at.petrak.hex.common.lib.HexSounds;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Sent server->client when the player finishes casting a spell.
 */
public record MsgNewSpellPatternAck(boolean quitCasting, List<String> stackDesc) {
    private static final String TAG_DESC = "desc";

    public static MsgNewSpellPatternAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        var quitCasting = buf.readBoolean();
        var descsTag = buf.readNbt().getList(TAG_DESC, Tag.TAG_STRING);
        var descs = new ArrayList<String>(descsTag.size());
        for (int i = 0; i < descsTag.size(); i++) {
            descs.add(descsTag.getString(i));
        }

        return new MsgNewSpellPatternAck(quitCasting, descs);
    }

    public void serialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        buf.writeBoolean(this.quitCasting);

        var descsCTag = new CompoundTag();
        var descsTag = new ListTag();
        for (String s : this.stackDesc) {
            descsTag.add(StringTag.valueOf(s));
        }
        descsCTag.put(TAG_DESC, descsTag);
        buf.writeNbt(descsCTag);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    var mc = Minecraft.getInstance();
                    if (this.quitCasting) {
                        // don't pay attention to the screen, so it also stops when we die
                        mc.getSoundManager().stop(HexSounds.CASTING_AMBIANCE.getId(), null);
                    }
                    var screen = Minecraft.getInstance().screen;
                    if (screen instanceof GuiSpellcasting) {
                        if (this.quitCasting) {
                            mc.setScreen(null);
                        } else {
                            var spellGui = (GuiSpellcasting) screen;
                            spellGui.setStackDescs(this.stackDesc);
                        }
                    }
                })
        );
        ctx.get().setPacketHandled(true);
    }

}
