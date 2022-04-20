package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.common.misc.Brainsweeping;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                IHateJava.handle(target);
            })
        );
        ctx.get().setPacketHandled(true);
    }

    private static class IHateJava {
        public static void handle(int target) {
            var level = Minecraft.getInstance().level;
            if (level != null) {
                Entity entity = level.getEntity(target);
                if (entity instanceof LivingEntity living) {
                    Brainsweeping.brainsweep(living);
                }
            }
        }
    }
}
