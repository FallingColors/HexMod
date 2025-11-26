package at.petrak.hexcasting.forge.network;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.player.AltioraAbility;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.client.Minecraft;
<<<<<<< HEAD
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
=======
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
>>>>>>> refs/remotes/slava/devel/port-1.21
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record MsgAltioraUpdateAck(@Nullable AltioraAbility altiora) implements CustomPacketPayload {
<<<<<<< HEAD
    public static final StreamCodec<FriendlyByteBuf, MsgAltioraUpdateAck> CODEC = CustomPacketPayload.codec(
            MsgAltioraUpdateAck::serialize,
            MsgAltioraUpdateAck::deserialize
    );
    public static final Type<MsgAltioraUpdateAck> ID = new Type<>(modLoc("altiora"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static MsgAltioraUpdateAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        var extant = buf.readBoolean();
        if (!extant) {
            return new MsgAltioraUpdateAck(null);
        }
        var grace = buf.readVarInt();
        return new MsgAltioraUpdateAck(new AltioraAbility(grace));
    }

    public void serialize(FriendlyByteBuf buf) {
        buf.writeBoolean(this.altiora != null);
        if (this.altiora != null) {
            buf.writeVarInt(this.altiora.gracePeriod());
        }
=======
    public static final CustomPacketPayload.Type<MsgAltioraUpdateAck> TYPE = new CustomPacketPayload.Type<>(HexAPI.modLoc("altiora"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgAltioraUpdateAck> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(AltioraAbility.STREAM_CODEC).map(
                    opt -> opt.orElse(null),
                    Optional::ofNullable
            ), MsgAltioraUpdateAck::altiora,
            MsgAltioraUpdateAck::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
>>>>>>> refs/remotes/slava/devel/port-1.21
    }

    public static void handle(MsgAltioraUpdateAck self) {
        Minecraft.getInstance().execute(() -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                IXplatAbstractions.INSTANCE.setAltiora(player, self.altiora);
            }
        });
    }
}
