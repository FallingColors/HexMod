package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.common.casting.operators.spells.sentinel.CapSentinel;
import at.petrak.hexcasting.common.lib.HexCapabilities;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent server->client to synchronize the status of the sentinel.
 */
public record MsgSentinelStatusUpdateAck(CapSentinel update) {
    public static MsgSentinelStatusUpdateAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        var tag = buf.readAnySizeNbt();
        var sentinel = new CapSentinel(false, false, Vec3.ZERO, 0);
        sentinel.deserializeNBT(tag);
        return new MsgSentinelStatusUpdateAck(sentinel);
    }

    public void serialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        buf.writeNbt(this.update.serializeNBT());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                var player = Minecraft.getInstance().player;
                var maybeCap = player.getCapability(HexCapabilities.SENTINEL).resolve();
                if (!maybeCap.isPresent()) {
                    return;
                }

                var cap = maybeCap.get();
                cap.hasSentinel = update().hasSentinel;
                cap.extendsRange = update().hasSentinel;
                cap.position = update().position;
                cap.color = update().color;
            })
        );
        ctx.get().setPacketHandled(true);
    }
}
