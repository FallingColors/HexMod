package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.api.casting.eval.ExecutionClientView;
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import at.petrak.hexcasting.common.lib.HexSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent server->client when the player finishes casting a spell.
 */
public record MsgNewSpellPatternS2C(ExecutionClientView info, int index) implements IMessage {
    public static final ResourceLocation ID = modLoc("pat_sc");
    public static final CustomPacketPayload.Type<MsgNewSpellPatternS2C> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, MsgNewSpellPatternS2C> STREAM_CODEC =
        StreamCodec.ofMember(MsgNewSpellPatternS2C::serialize, MsgNewSpellPatternS2C::deserialize);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgNewSpellPatternS2C deserialize(FriendlyByteBuf buf) {

        var isStackEmpty = buf.readBoolean();
        var resolutionType = buf.readEnum(ResolvedPatternType.class);
        var index = buf.readInt();

        var stack = buf.readList(fbb -> fbb.readNbt());
        var raven = buf.readOptional(fbb -> fbb.readNbt()).orElse(null);

        return new MsgNewSpellPatternS2C(
            new ExecutionClientView(isStackEmpty, resolutionType, stack, raven), index
        );
    }

    @Override
    public void serialize(FriendlyByteBuf buf) {
        buf.writeBoolean(this.info.isStackClear());
        buf.writeEnum(this.info.getResolutionType());
        buf.writeInt(this.index);

        buf.writeCollection(this.info.getStackDescs(), (fbb, tag) -> fbb.writeNbt(tag));
        buf.writeOptional(Optional.ofNullable(this.info.getRavenmind()), (fbb, tag) -> fbb.writeNbt(tag));
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
