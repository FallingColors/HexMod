package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.api.misc.FrozenColorizer;
import at.petrak.hexcasting.api.player.HexPlayerDataHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent server->client to synchronize the status of the sentinel.
 */
public record MsgColorizerUpdateAck(FrozenColorizer update) implements IMessage {
    public static final ResourceLocation ID = modLoc("color");

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgColorizerUpdateAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        var tag = buf.readAnySizeNbt();
        var colorizer = FrozenColorizer.deserialize(tag);
        return new MsgColorizerUpdateAck(colorizer);
    }

    @Override
    public void serialize(FriendlyByteBuf buf) {
        buf.writeNbt(this.update.serialize());
    }

    public static void handle(MsgColorizerUpdateAck self) {
        Minecraft.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                var player = Minecraft.getInstance().player;
                if (player != null) {
                    HexPlayerDataHelper.setColorizer(player, self.update());
                }
            }
        });
    }
}
