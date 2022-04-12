package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.api.player.Sentinel;
import at.petrak.hexcasting.api.player.HexPlayerDataHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent server->client to synchronize the status of the sentinel.
 */
public record MsgSentinelStatusUpdateAck(Sentinel update) {
    public static MsgSentinelStatusUpdateAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        var exists = buf.readBoolean();
        var greater = buf.readBoolean();
        var origin = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        var dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation());

        var sentinel = new Sentinel(exists, greater, origin, dimension);
        return new MsgSentinelStatusUpdateAck(sentinel);
    }

    public void serialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        buf.writeBoolean(update.hasSentinel());
        buf.writeBoolean(update.extendsRange());
        buf.writeDouble(update.position().x);
        buf.writeDouble(update.position().y);
        buf.writeDouble(update.position().z);
        buf.writeResourceLocation(update.dimension().location());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                var player = Minecraft.getInstance().player;
                if (player != null) {
                    HexPlayerDataHelper.setSentinel(player, update);
                }
            })
        );
        ctx.get().setPacketHandled(true);
    }
}
