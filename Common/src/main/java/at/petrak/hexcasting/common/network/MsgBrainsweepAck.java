package at.petrak.hexcasting.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent server->client to synchronize the status of a brainswept mob.
 */
public record MsgBrainsweepAck(int target) {
    public static MsgBrainsweepAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        var target = buf.readInt();
        return new MsgBrainsweepAck(target);
    }

    public void serialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        buf.writeInt(target);
    }

    public static MsgBrainsweepAck of(Entity target) {
        return new MsgBrainsweepAck(target.getId());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientPacketHandler handler = new ClientPacketHandler(this);
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> handler::brainsweep);
        });
        ctx.get().setPacketHandled(true);
    }
}
