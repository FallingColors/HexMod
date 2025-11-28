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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
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
    public static final CustomPacketPayload.Type<MsgNewSpellPatternS2C> TYPE = new CustomPacketPayload.Type<>(HexAPI.modLoc("pat_sc"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgNewSpellPatternS2C> STREAM_CODEC = StreamCodec.composite(
            ExecutionClientView.getSTREAM_CODEC(), MsgNewSpellPatternS2C::info,
            ByteBufCodecs.VAR_INT, MsgNewSpellPatternS2C::index,
            MsgNewSpellPatternS2C::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
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
