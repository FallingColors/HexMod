package at.petrak.hexcasting.forge.network;

import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.msgs.IMessage;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent server->client to synchronize the status of the sentinel.
 */
public record MsgPigmentUpdateAck(FrozenPigment update) implements IMessage {
    public static final ResourceLocation ID = modLoc("color");
    public static final CustomPacketPayload.Type<MsgPigmentUpdateAck> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, MsgPigmentUpdateAck> STREAM_CODEC =
        StreamCodec.ofMember(MsgPigmentUpdateAck::serialize, MsgPigmentUpdateAck::deserialize);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgPigmentUpdateAck deserialize(FriendlyByteBuf buf) {
        var tag = buf.readNbt();
        var colorizer = FrozenPigment.fromNBT(tag != null ? tag : new net.minecraft.nbt.CompoundTag());
        return new MsgPigmentUpdateAck(colorizer);
    }

    @Override
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
