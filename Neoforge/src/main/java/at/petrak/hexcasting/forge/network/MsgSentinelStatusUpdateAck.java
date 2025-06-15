package at.petrak.hexcasting.forge.network;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.player.Sentinel;
import at.petrak.hexcasting.common.msgs.IMessage;
import at.petrak.hexcasting.common.msgs.MsgBlinkS2C;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.paucal.api.PaucalCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
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
