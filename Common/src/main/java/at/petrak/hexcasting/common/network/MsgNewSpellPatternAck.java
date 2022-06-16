package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.api.spell.casting.ControllerInfo;
import at.petrak.hexcasting.api.spell.casting.ResolvedPatternType;
import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import at.petrak.hexcasting.common.lib.HexSounds;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent server->client when the player finishes casting a spell.
 */
public record MsgNewSpellPatternAck(ControllerInfo info) implements IMessage {
    public static final ResourceLocation ID = modLoc("pat_sc");

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgNewSpellPatternAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        var wasSpellCast = buf.readBoolean();
        var isStackEmpty = buf.readBoolean();
        var resolutionType = buf.readEnum(ResolvedPatternType.class);

        var stack = buf.readList(FriendlyByteBuf::readNbt);
        var parens = buf.readList(FriendlyByteBuf::readNbt);
        var raven = buf.readOptional(FriendlyByteBuf::readNbt).orElse(null);

        var parenCount = buf.readVarInt();

        return new MsgNewSpellPatternAck(
            new ControllerInfo(wasSpellCast, isStackEmpty, resolutionType, stack, parens, raven, parenCount)
        );
    }

    @Override
    public void serialize(FriendlyByteBuf buf) {
        buf.writeBoolean(this.info.getMakesCastSound());
        buf.writeBoolean(this.info.isStackClear());
        buf.writeEnum(this.info.getResolutionType());

        buf.writeCollection(this.info.getStack(), FriendlyByteBuf::writeNbt);
        buf.writeCollection(this.info.getParenthesized(), FriendlyByteBuf::writeNbt);
        buf.writeOptional(Optional.ofNullable(this.info.getRavenmind()), FriendlyByteBuf::writeNbt);

        buf.writeVarInt(this.info.getParenCount());
    }

    public static void handle(MsgNewSpellPatternAck self) {
        Minecraft.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                var mc = Minecraft.getInstance();
                if (self.info().isStackClear()) {
                    // don't pay attention to the screen, so it also stops when we die
                    mc.getSoundManager().stop(HexSounds.CASTING_AMBIANCE.getLocation(), null);
                }
                var screen = Minecraft.getInstance().screen;
                if (screen instanceof GuiSpellcasting spellGui) {
                    if (self.info().isStackClear()) {
                        mc.setScreen(null);
                    } else {
                        spellGui.recvServerUpdate(self.info());
                    }
                }
            }
        });
    }
}
