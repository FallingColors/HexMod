package at.petrak.hexcasting.forge.network;

import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.msgs.IMessage;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent server->client to synchronize the status of the sentinel.
 */
public record MsgPigmentUpdateAck(FrozenPigment update) implements IMessage {
    public static final ResourceLocation ID = modLoc("color");

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgPigmentUpdateAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        var tag = buf.readAnySizeNbt();
        var colorizer = FrozenPigment.fromNBT(tag);
        return new MsgPigmentUpdateAck(colorizer);
    }

    @Override
    public void serialize(FriendlyByteBuf buf) {
        buf.writeNbt(this.update.serializeToNBT());
    }

    public static void handle(MsgPigmentUpdateAck self) {
        Minecraft.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                var player = Minecraft.getInstance().player;
                if (player != null) {
                    IXplatAbstractions.INSTANCE.setPigment(player, self.update());
                }
            }
        });
    }
}
