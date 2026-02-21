package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record MsgClearSpiralPatternsS2C(UUID playerUUID) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MsgClearSpiralPatternsS2C> TYPE = new CustomPacketPayload.Type<>(HexAPI.modLoc("clr_spi_pats_sc"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgClearSpiralPatternsS2C> STREAM_CODEC = UUIDUtil.STREAM_CODEC.map(
            MsgClearSpiralPatternsS2C::new,
            MsgClearSpiralPatternsS2C::playerUUID
    ).mapStream(b -> b);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle() {
        Handler.handle(this);
    }

    public static final class Handler {

        public static void handle(MsgClearSpiralPatternsS2C self) {
            Minecraft.getInstance().execute(() -> {
                var mc = Minecraft.getInstance();
                assert mc.level != null;
                var player = mc.level.getPlayerByUUID(self.playerUUID);
                var stack = IClientXplatAbstractions.INSTANCE.getClientCastingStack(player);
                stack.slowClear();
            });
        }
    }
}
