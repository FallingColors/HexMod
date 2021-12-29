package at.petrak.hex.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent server->client to synchronize OpAddMotion when the target is a player.
 */
public record MsgAddMotionAck(Vec3 addedMotion) {
    public static MsgAddMotionAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        var x = buf.readDouble();
        var y = buf.readDouble();
        var z = buf.readDouble();
        return new MsgAddMotionAck(new Vec3(x, y, z));
    }

    public void serialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        buf.writeDouble(this.addedMotion.x);
        buf.writeDouble(this.addedMotion.y);
        buf.writeDouble(this.addedMotion.z);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                var player = Minecraft.getInstance().player;
                var motion = player.getDeltaMovement();
                player.setDeltaMovement(motion.add(this.addedMotion));
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
