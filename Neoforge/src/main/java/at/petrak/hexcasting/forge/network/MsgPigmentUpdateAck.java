package at.petrak.hexcasting.forge.network;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

/**
 * Sent server->client to synchronize the status of the sentinel.
 */
public record MsgPigmentUpdateAck(int playerId, FrozenPigment update) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MsgPigmentUpdateAck> TYPE = new CustomPacketPayload.Type<>(HexAPI.modLoc("color"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgPigmentUpdateAck> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, MsgPigmentUpdateAck::playerId,
            FrozenPigment.STREAM_CODEC, MsgPigmentUpdateAck::update,
            MsgPigmentUpdateAck::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle() {
        Handler.handle(this);
    }

    public static final class Handler {

        public static void handle(MsgPigmentUpdateAck self) {
            Minecraft.getInstance().execute(() -> {
                if (Minecraft.getInstance().level == null) return;
                var selfEntity = Minecraft.getInstance().level.getEntity(self.playerId);
                if (!(selfEntity instanceof Player player)) return;
                IXplatAbstractions.INSTANCE.setPigment(player, self.update);
            });
        }
    }
}
