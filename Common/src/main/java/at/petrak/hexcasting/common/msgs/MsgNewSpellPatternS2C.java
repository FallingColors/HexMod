package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.api.casting.eval.ExecutionClientView;
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
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
public record MsgNewSpellPatternS2C(ExecutionClientView info, int index) implements IMessage {
    public static final ResourceLocation ID = modLoc("pat_sc");

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgNewSpellPatternS2C deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        var isStackEmpty = buf.readBoolean();
        var resolutionType = buf.readEnum(ResolvedPatternType.class);
        var index = buf.readInt();

        var stack = buf.readList(FriendlyByteBuf::readNbt);
        var raven = buf.readOptional(FriendlyByteBuf::readNbt).orElse(null);

        return new MsgNewSpellPatternS2C(
            new ExecutionClientView(isStackEmpty, resolutionType, stack, raven), index
        );
    }

    @Override
    public void serialize(FriendlyByteBuf buf) {
        buf.writeBoolean(this.info.isStackClear());
        buf.writeEnum(this.info.getResolutionType());
        buf.writeInt(this.index);

        buf.writeCollection(this.info.getStackDescs(), FriendlyByteBuf::writeNbt);
        buf.writeOptional(Optional.ofNullable(this.info.getRavenmind()), FriendlyByteBuf::writeNbt);
    }

    public static void handle(MsgNewSpellPatternS2C self) {
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
                    spellGui.recvServerUpdate(self.info(), self.index());
                }
            }
        });
    }
}
