package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import at.petrak.hexcasting.common.casting.CastingHarness;
import at.petrak.hexcasting.common.lib.HexSounds;
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
public record MsgNewSpellPatternAck(CastingHarness.QuitStatus state, List<String> stackDesc) {
    private static final String TAG_DESC = "desc";

    public static MsgNewSpellPatternAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        var state = CastingHarness.QuitStatus.values()[buf.readInt()];
        var descsTag = buf.readNbt().getList(TAG_DESC, Tag.TAG_STRING);
        var descs = new ArrayList<String>(descsTag.size());
        for (int i = 0; i < descsTag.size(); i++) {
            descs.add(descsTag.getString(i));
        }

        return new MsgNewSpellPatternAck(state, descs);
    }

    public void serialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        buf.writeInt(this.state.ordinal());

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
                if (this.state == CastingHarness.QuitStatus.QUIT) {
                    // don't pay attention to the screen, so it also stops when we die
                    mc.getSoundManager().stop(HexSounds.CASTING_AMBIANCE.getId(), null);
                }
                var screen = Minecraft.getInstance().screen;
                if (screen instanceof GuiSpellcasting spellGui) {
                    if (this.state == CastingHarness.QuitStatus.QUIT) {
                        mc.setScreen(null);
                    } else {
                        spellGui.recvServerUpdate(this.stackDesc,
                            this.state == CastingHarness.QuitStatus.LAST_PATTERN_INVALID);
                    }
                }
            })
        );
        ctx.get().setPacketHandled(true);
    }

}
