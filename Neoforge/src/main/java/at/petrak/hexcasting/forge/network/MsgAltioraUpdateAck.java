package at.petrak.hexcasting.forge.network;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.player.AltioraAbility;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record MsgAltioraUpdateAck(int playerId, @Nullable AltioraAbility altiora) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MsgAltioraUpdateAck> TYPE = new CustomPacketPayload.Type<>(HexAPI.modLoc("altiora"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgAltioraUpdateAck> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, MsgAltioraUpdateAck::playerId,
            ByteBufCodecs.optional(AltioraAbility.STREAM_CODEC).map(
                    opt -> opt.orElse(null),
                    Optional::ofNullable
            ), MsgAltioraUpdateAck::altiora,
            MsgAltioraUpdateAck::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle() {
        Handler.handle(this);
    }

    public static final class Handler {

        public static void handle(MsgAltioraUpdateAck self) {
            Minecraft.getInstance().execute(() -> {
                if (Minecraft.getInstance().level == null) return;
                var selfEntity = Minecraft.getInstance().level.getEntity(self.playerId);
                if (!(selfEntity instanceof Player player)) return;
                IXplatAbstractions.INSTANCE.setAltiora(player, self.altiora);
            });
        }
    }
}
