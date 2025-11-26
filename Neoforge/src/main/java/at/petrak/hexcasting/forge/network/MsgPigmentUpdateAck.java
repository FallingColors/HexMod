package at.petrak.hexcasting.forge.network;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
<<<<<<< HEAD
=======
import at.petrak.hexcasting.common.msgs.IMessage;
import at.petrak.hexcasting.common.msgs.MsgBlinkS2C;
>>>>>>> refs/remotes/slava/devel/port-1.21
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.paucal.api.PaucalCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
<<<<<<< HEAD
=======
import net.minecraft.network.RegistryFriendlyByteBuf;
>>>>>>> refs/remotes/slava/devel/port-1.21
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent server->client to synchronize the status of the sentinel.
 */
public record MsgPigmentUpdateAck(FrozenPigment update) implements CustomPacketPayload {
<<<<<<< HEAD
    public static final StreamCodec<FriendlyByteBuf, MsgPigmentUpdateAck> CODEC = CustomPacketPayload.codec(MsgPigmentUpdateAck::serialize, MsgPigmentUpdateAck::deserialize);

    public static final Type<MsgPigmentUpdateAck> ID = new Type<>(modLoc("color"));

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }

    public static MsgPigmentUpdateAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        var tag = buf.readNbt();
        var colorizer = FrozenPigment.fromNBT(tag);
        return new MsgPigmentUpdateAck(colorizer);
    }

    public void serialize(FriendlyByteBuf buf) {
        buf.writeNbt(this.update.serializeToNBT());
=======
    public static final CustomPacketPayload.Type<MsgPigmentUpdateAck> TYPE = new CustomPacketPayload.Type<>(HexAPI.modLoc("color"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgPigmentUpdateAck> STREAM_CODEC = StreamCodec.composite(
            FrozenPigment.STREAM_CODEC, MsgPigmentUpdateAck::update,
            MsgPigmentUpdateAck::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
>>>>>>> refs/remotes/slava/devel/port-1.21
    }

    public static void handle(MsgPigmentUpdateAck self) {
        Minecraft.getInstance().execute(() -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                IXplatAbstractions.INSTANCE.setPigment(player, self.update());
            }
        });
    }


}
