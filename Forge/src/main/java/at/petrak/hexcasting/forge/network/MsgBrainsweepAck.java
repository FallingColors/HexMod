package at.petrak.hexcasting.forge.network;

import at.petrak.hexcasting.common.msgs.IMessage;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent server->client to synchronize the status of a brainswept mob.
 */
public record MsgBrainsweepAck(int target) implements IMessage {
    public static final ResourceLocation ID = modLoc("sweep");
    public static final CustomPacketPayload.Type<MsgBrainsweepAck> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, MsgBrainsweepAck> STREAM_CODEC =
        StreamCodec.ofMember(MsgBrainsweepAck::serialize, MsgBrainsweepAck::deserialize);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgBrainsweepAck deserialize(FriendlyByteBuf buf) {

        var target = buf.readInt();
        return new MsgBrainsweepAck(target);
    }

    @Override
    public void serialize(FriendlyByteBuf buf) {
        buf.writeInt(target);
    }

    public static MsgBrainsweepAck of(Entity target) {
        return new MsgBrainsweepAck(target.getId());
    }

    public static void handle(MsgBrainsweepAck msg) {
        Minecraft.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                var level = Minecraft.getInstance().level;
                if (level != null) {
                    Entity entity = level.getEntity(msg.target());
                    if (entity instanceof Mob living) {
                        IXplatAbstractions.INSTANCE.setBrainsweepAddlData(living);
                    }
                }
            }
        });
    }
}
