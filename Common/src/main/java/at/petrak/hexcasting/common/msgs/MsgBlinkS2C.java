package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.eval.ExecutionClientView;
import at.petrak.paucal.api.PaucalCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
<<<<<<< HEAD
=======
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
>>>>>>> refs/remotes/slava/devel/port-1.21
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent server->client to synchronize OpBlink when the target is a player.
 */
public record MsgBlinkS2C(Vec3 addedPosition) implements CustomPacketPayload {
<<<<<<< HEAD
    public static final StreamCodec<FriendlyByteBuf, MsgBlinkS2C> CODEC = CustomPacketPayload.codec(MsgBlinkS2C::serialize, MsgBlinkS2C::deserialize);
    public static final Type<MsgBlinkS2C> ID = new Type<>(modLoc("blink"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static MsgBlinkS2C deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        var x = buf.readDouble();
        var y = buf.readDouble();
        var z = buf.readDouble();
        return new MsgBlinkS2C(new Vec3(x, y, z));
    }

    public void serialize(FriendlyByteBuf buf) {
        buf.writeDouble(this.addedPosition.x);
        buf.writeDouble(this.addedPosition.y);
        buf.writeDouble(this.addedPosition.z);
=======
    public static final CustomPacketPayload.Type<MsgBlinkS2C> TYPE = new CustomPacketPayload.Type<>(HexAPI.modLoc("blink"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgBlinkS2C> STREAM_CODEC = StreamCodec.composite(
            PaucalCodecs.VEC3, MsgBlinkS2C::addedPosition,
            MsgBlinkS2C::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
>>>>>>> refs/remotes/slava/devel/port-1.21
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
