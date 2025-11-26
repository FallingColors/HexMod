package at.petrak.hexcasting.forge.network;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.player.Sentinel;
<<<<<<< HEAD
=======
import at.petrak.hexcasting.common.msgs.IMessage;
import at.petrak.hexcasting.common.msgs.MsgBlinkS2C;
>>>>>>> refs/remotes/slava/devel/port-1.21
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.paucal.api.PaucalCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
<<<<<<< HEAD
=======
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
>>>>>>> refs/remotes/slava/devel/port-1.21
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

import java.util.Optional;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent server->client to synchronize the status of the sentinel.
 */
public record MsgSentinelStatusUpdateAck(@Nullable Sentinel update) implements CustomPacketPayload {
<<<<<<< HEAD
    public static final StreamCodec<FriendlyByteBuf, MsgSentinelStatusUpdateAck> CODEC = CustomPacketPayload.codec(
            MsgSentinelStatusUpdateAck::serialize,
            MsgSentinelStatusUpdateAck::deserialize
    );
    public static final Type<MsgSentinelStatusUpdateAck> ID = new Type<>(modLoc("sntnl"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static MsgSentinelStatusUpdateAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        var exists = buf.readBoolean();
        if (!exists) {
            return new MsgSentinelStatusUpdateAck(null);
        }

        var greater = buf.readBoolean();
        var origin = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        var dimension = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());

        var sentinel = new Sentinel(greater, origin, dimension);
        return new MsgSentinelStatusUpdateAck(sentinel);
    }

    public void serialize(FriendlyByteBuf buf) {
        if (update == null) {
            buf.writeBoolean(false);
            return;
        }

        buf.writeBoolean(true);
        buf.writeBoolean(update.extendsRange());
        buf.writeDouble(update.position().x);
        buf.writeDouble(update.position().y);
        buf.writeDouble(update.position().z);
        buf.writeResourceLocation(update.dimension().location());
=======
    public static final CustomPacketPayload.Type<MsgSentinelStatusUpdateAck> TYPE = new CustomPacketPayload.Type<>(HexAPI.modLoc("sntnl"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgSentinelStatusUpdateAck> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(Sentinel.STREAM_CODEC).map(
                opt -> opt.orElse(null),
                Optional::ofNullable
            ), MsgSentinelStatusUpdateAck::update,
            MsgSentinelStatusUpdateAck::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
>>>>>>> refs/remotes/slava/devel/port-1.21
    }

    public static void handle(MsgSentinelStatusUpdateAck self) {
        Minecraft.getInstance().execute(() -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                IXplatAbstractions.INSTANCE.setSentinel(player, self.update());
            }
        });
    }
}
