package at.petrak.hexcasting.forge.network;

import at.petrak.hexcasting.api.player.AltioraAbility;
import at.petrak.hexcasting.common.msgs.IMessage;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public record MsgAltioraUpdateAck(@Nullable AltioraAbility altiora) implements IMessage {
    public static final ResourceLocation ID = modLoc("altiora");
    public static final CustomPacketPayload.Type<MsgAltioraUpdateAck> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, MsgAltioraUpdateAck> STREAM_CODEC =
        StreamCodec.ofMember(MsgAltioraUpdateAck::serialize, MsgAltioraUpdateAck::deserialize);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgAltioraUpdateAck deserialize(FriendlyByteBuf buf) {

        var extant = buf.readBoolean();
        if (!extant) {
            return new MsgAltioraUpdateAck(null);
        }
        var grace = buf.readVarInt();
        return new MsgAltioraUpdateAck(new AltioraAbility(grace));
    }

    @Override
    public void serialize(FriendlyByteBuf buf) {
        buf.writeBoolean(this.altiora != null);
        if (this.altiora != null) {
            buf.writeVarInt(this.altiora.gracePeriod());
        }
    }

    public static void handle(MsgAltioraUpdateAck self) {
        Minecraft.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                var player = Minecraft.getInstance().player;
                if (player != null) {
                    IXplatAbstractions.INSTANCE.setAltiora(player, self.altiora);
                }
            }
        });
    }
}
