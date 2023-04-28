package at.petrak.hexcasting.common.msgs;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent server->client to synchronize OpBlink when the target is a player.
 */
public record MsgBlinkS2C(Vec3 addedPosition) implements IMessage {
    public static final ResourceLocation ID = modLoc("blink");

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgBlinkS2C deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        var x = buf.readDouble();
        var y = buf.readDouble();
        var z = buf.readDouble();
        return new MsgBlinkS2C(new Vec3(x, y, z));
    }

    @Override
    public void serialize(FriendlyByteBuf buf) {
        buf.writeDouble(this.addedPosition.x);
        buf.writeDouble(this.addedPosition.y);
        buf.writeDouble(this.addedPosition.z);
    }

    public static void handle(MsgBlinkS2C self) {
        Minecraft.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                var player = Minecraft.getInstance().player;
                player.setPos(player.position().add(self.addedPosition()));
            }
        });
    }
}
