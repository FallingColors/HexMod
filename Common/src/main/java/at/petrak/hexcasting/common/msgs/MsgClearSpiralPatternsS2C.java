package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public record MsgClearSpiralPatternsS2C(UUID playerUUID) implements IMessage {
    public static final ResourceLocation ID = modLoc("clr_spi_pats_sc");
    public static final CustomPacketPayload.Type<MsgClearSpiralPatternsS2C> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, MsgClearSpiralPatternsS2C> STREAM_CODEC =
        StreamCodec.ofMember(MsgClearSpiralPatternsS2C::serialize, MsgClearSpiralPatternsS2C::deserialize);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgClearSpiralPatternsS2C deserialize(FriendlyByteBuf buf) {

        var player = buf.readUUID();

        return new MsgClearSpiralPatternsS2C(player);
    }

    @Override
    public void serialize(FriendlyByteBuf buf) {
        buf.writeUUID(playerUUID);
    }

    public static void handle(MsgClearSpiralPatternsS2C self) {
        Minecraft.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                var mc = Minecraft.getInstance();
                assert mc.level != null;
                var player = mc.level.getPlayerByUUID(self.playerUUID);
                var stack = IClientXplatAbstractions.INSTANCE.getClientCastingStack(player);
                stack.slowClear();
            }
        });
    }
}
