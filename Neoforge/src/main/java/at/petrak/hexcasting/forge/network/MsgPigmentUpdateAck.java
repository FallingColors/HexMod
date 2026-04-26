package at.petrak.hexcasting.forge.network;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Sent server->client to synchronize the status of the sentinel.
 */
public record MsgPigmentUpdateAck(FrozenPigment update) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MsgPigmentUpdateAck> TYPE = new CustomPacketPayload.Type<>(HexAPI.modLoc("color"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgPigmentUpdateAck> STREAM_CODEC = StreamCodec.composite(
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
                var player = Minecraft.getInstance().player;
                if (player != null) {
                    IXplatAbstractions.INSTANCE.setPigment(player, self.update());
                }
            });
        }
    }
}
