package at.petrak.hexcasting.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent server->client to synchronize OpBlink when the target is a player.
 */
public record MsgBlinkAck(Vec3 addedPosition) {
    public static MsgBlinkAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        var x = buf.readDouble();
        var y = buf.readDouble();
        var z = buf.readDouble();
        return new MsgBlinkAck(new Vec3(x, y, z));
    }

    public void serialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        buf.writeDouble(this.addedPosition.x);
        buf.writeDouble(this.addedPosition.y);
        buf.writeDouble(this.addedPosition.z);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientPacketHandler handler = new ClientPacketHandler(this);
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> handler::blink);
        });
        ctx.get().setPacketHandled(true);
    }
}
