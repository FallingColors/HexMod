package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.common.casting.colors.CapPreferredColorizer;
import at.petrak.hexcasting.common.casting.colors.FrozenColorizer;
import at.petrak.hexcasting.common.lib.HexCapabilities;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent server->client to synchronize the status of the sentinel.
 */
public record MsgColorizerUpdateAck(CapPreferredColorizer update) {
    public static MsgColorizerUpdateAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        var tag = buf.readAnySizeNbt();
        var colorizer = new CapPreferredColorizer(FrozenColorizer.DEFAULT);
        colorizer.deserializeNBT(tag);
        return new MsgColorizerUpdateAck(colorizer);
    }

    public void serialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        buf.writeNbt(this.update.serializeNBT());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                var player = Minecraft.getInstance().player;
                var maybeCap = player.getCapability(HexCapabilities.PREFERRED_COLORIZER).resolve();
                if (!maybeCap.isPresent()) {
                    return;
                }

                var cap = maybeCap.get();
                cap.colorizer = update().colorizer;
            })
        );
        ctx.get().setPacketHandled(true);
    }
}
