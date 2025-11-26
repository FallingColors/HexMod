package at.petrak.hexcasting.forge.network;

<<<<<<< HEAD
=======
import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.player.Sentinel;
import at.petrak.hexcasting.common.msgs.IMessage;
>>>>>>> refs/remotes/slava/devel/port-1.21
import at.petrak.hexcasting.xplat.IXplatAbstractions;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import java.util.Optional;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent server->client to synchronize the status of a brainswept mob.
 */
public record MsgBrainsweepAck(int target) implements CustomPacketPayload {
<<<<<<< HEAD
    public static final StreamCodec<FriendlyByteBuf, MsgBrainsweepAck> CODEC = CustomPacketPayload.codec(
            MsgBrainsweepAck::serialize,
            MsgBrainsweepAck::deserialize
    );
    public static final Type<MsgBrainsweepAck> ID = new Type<>(modLoc("sweep"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static MsgBrainsweepAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        var target = buf.readInt();
        return new MsgBrainsweepAck(target);
    }

    public void serialize(FriendlyByteBuf buf) {
        buf.writeInt(target);
=======
    public static final CustomPacketPayload.Type<MsgBrainsweepAck> TYPE = new CustomPacketPayload.Type<>(HexAPI.modLoc("sweep"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgBrainsweepAck> STREAM_CODEC = ByteBufCodecs.INT.map(
            MsgBrainsweepAck::new,
            MsgBrainsweepAck::target
    ).mapStream(b -> b);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
>>>>>>> refs/remotes/slava/devel/port-1.21
    }

    public static MsgBrainsweepAck of(Entity target) {
        return new MsgBrainsweepAck(target.getId());
    }

    public static void handle(MsgBrainsweepAck msg) {
        Minecraft.getInstance().execute(() -> {
            var level = Minecraft.getInstance().level;
            if (level != null) {
                Entity entity = level.getEntity(msg.target());
                if (entity instanceof Mob living) {
                    IXplatAbstractions.INSTANCE.setBrainsweepAddlData(living);
                }
            }
        });
    }
}
