package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public record MsgClearSpiralPatternsS2C(UUID playerUUID) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, MsgClearSpiralPatternsS2C> CODEC = CustomPacketPayload.codec(MsgClearSpiralPatternsS2C::serialize, MsgClearSpiralPatternsS2C::deserialize);
    public static final CustomPacketPayload.Type<MsgClearSpiralPatternsS2C> ID = new CustomPacketPayload.Type<>(modLoc("clr_spi_pats_sc"));

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static MsgClearSpiralPatternsS2C deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        var player = buf.readUUID();

        return new MsgClearSpiralPatternsS2C(player);
    }

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
