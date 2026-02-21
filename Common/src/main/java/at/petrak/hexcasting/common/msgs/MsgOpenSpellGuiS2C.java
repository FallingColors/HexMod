package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.eval.ResolvedPattern;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Sent server->client when the player opens the spell gui to request the server provide the current stack.
 */
public record MsgOpenSpellGuiS2C(InteractionHand hand, List<ResolvedPattern> patterns,
                                 List<Iota> stack,
                                 @Nullable
                                 CompoundTag ravenmind,
                                 int parenCount
)
    implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MsgOpenSpellGuiS2C> TYPE = new CustomPacketPayload.Type<>(HexAPI.modLoc("cgui"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgOpenSpellGuiS2C> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL.map(
                    isMain -> isMain ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                    hand -> hand == InteractionHand.MAIN_HAND
            ), MsgOpenSpellGuiS2C::hand,
            ResolvedPattern.STREAM_CODEC.apply(ByteBufCodecs.list()), MsgOpenSpellGuiS2C::patterns,
            IotaType.TYPED_STREAM_CODEC.apply(ByteBufCodecs.list()), MsgOpenSpellGuiS2C::stack,
            ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG).map(
                    opt -> opt.orElse(null),
                    Optional::ofNullable
            ), MsgOpenSpellGuiS2C::ravenmind,
            ByteBufCodecs.VAR_INT, MsgOpenSpellGuiS2C::parenCount,
            MsgOpenSpellGuiS2C::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle() {
        Handler.handle(this);
    }

    public static final class Handler {

        public static void handle(MsgOpenSpellGuiS2C msg) {
            Minecraft.getInstance().execute(() -> {
                var mc = Minecraft.getInstance();
                mc.setScreen(
                        new GuiSpellcasting(msg.hand(), msg.patterns(), msg.stack, msg.ravenmind,
                                msg.parenCount));
            });
        }
    }
}
