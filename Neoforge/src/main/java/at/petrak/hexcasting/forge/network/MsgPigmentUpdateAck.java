package at.petrak.hexcasting.forge.network;

import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent server->client to synchronize the status of the sentinel.
 */
public record MsgPigmentUpdateAck(FrozenPigment update) implements CustomPacketPayload {
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
    }

    public static void handle(MsgPigmentUpdateAck self) {
        Minecraft.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                var player = Minecraft.getInstance().player;
                if (player != null) {
                    IXplatAbstractions.INSTANCE.setPigment(player, self.update());
                }
            }
        });
    }


}
