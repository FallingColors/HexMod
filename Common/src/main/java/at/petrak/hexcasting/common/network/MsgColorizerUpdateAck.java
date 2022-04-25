package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.api.misc.FrozenColorizer;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent server->client to synchronize the status of the sentinel.
 */
public record MsgColorizerUpdateAck(FrozenColorizer update) {
    public static MsgColorizerUpdateAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        var tag = buf.readAnySizeNbt();
        var colorizer = FrozenColorizer.deserialize(tag);
        return new MsgColorizerUpdateAck(colorizer);
    }

    public void serialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        buf.writeNbt(this.update.serialize());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientPacketHandler handler = new ClientPacketHandler(this);
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> handler::updateColorizer);
        });
        ctx.get().setPacketHandled(true);
    }
}
