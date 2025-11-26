package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.eval.ExecutionClientView;
import at.petrak.hexcasting.api.casting.eval.ResolvedPattern;
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import at.petrak.hexcasting.common.lib.HexSounds;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
<<<<<<< HEAD
=======
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
>>>>>>> refs/remotes/slava/devel/port-1.21
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

import java.util.Optional;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent server->client when the player finishes casting a spell.
 */
public record MsgNewSpellPatternS2C(ExecutionClientView info, int index) implements CustomPacketPayload {
<<<<<<< HEAD
    public static final StreamCodec<FriendlyByteBuf, MsgNewSpellPatternS2C> CODEC = CustomPacketPayload.codec(MsgNewSpellPatternS2C::serialize, MsgNewSpellPatternS2C::deserialize);
    public static final Type<MsgNewSpellPatternS2C> ID = new Type<>(modLoc("pat_sc"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static MsgNewSpellPatternS2C deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        var isStackEmpty = buf.readBoolean();
        var resolutionType = buf.readEnum(ResolvedPatternType.class);
        var index = buf.readInt();

        var stack = buf.readList(bu -> bu.readNbt());
        var raven = buf.readOptional(bu -> bu.readNbt()).orElse(null);

        return new MsgNewSpellPatternS2C(
            new ExecutionClientView(isStackEmpty, resolutionType, stack, raven), index
        );
    }

    public void serialize(FriendlyByteBuf buf) {
        buf.writeBoolean(this.info.isStackClear());
        buf.writeEnum(this.info.getResolutionType());
        buf.writeInt(this.index);

        buf.writeCollection(this.info.getStackDescs(), (b, t) -> b.writeNbt(t));
        buf.writeOptional(Optional.ofNullable(this.info.getRavenmind()), (b, t) -> b.writeNbt(t));
=======
    public static final CustomPacketPayload.Type<MsgNewSpellPatternS2C> TYPE = new CustomPacketPayload.Type<>(HexAPI.modLoc("pat_sc"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgNewSpellPatternS2C> STREAM_CODEC = StreamCodec.composite(
            ExecutionClientView.getSTREAM_CODEC(), MsgNewSpellPatternS2C::info,
            ByteBufCodecs.VAR_INT, MsgNewSpellPatternS2C::index,
            MsgNewSpellPatternS2C::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
>>>>>>> refs/remotes/slava/devel/port-1.21
    }

    public static void handle(MsgNewSpellPatternS2C self) {
        Minecraft.getInstance().execute(() -> {
            var mc = Minecraft.getInstance();
            if (self.info().isStackClear()) {
                // don't pay attention to the screen, so it also stops when we die
                mc.getSoundManager().stop(HexSounds.CASTING_AMBIANCE.getLocation(), null);
            }
            var screen = Minecraft.getInstance().screen;
            if (screen instanceof GuiSpellcasting spellGui) {
                spellGui.recvServerUpdate(self.info(), self.index());
            }
        });
    }
}
