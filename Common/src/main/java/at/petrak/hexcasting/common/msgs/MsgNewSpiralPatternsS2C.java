package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;
import java.util.UUID;

public record MsgNewSpiralPatternsS2C(UUID playerUUID, List<HexPattern> patterns, int lifetime) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MsgNewSpiralPatternsS2C> TYPE = new CustomPacketPayload.Type<>(HexAPI.modLoc("spi_pats_sc"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgNewSpiralPatternsS2C> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, MsgNewSpiralPatternsS2C::playerUUID,
            HexPattern.STREAM_CODEC.apply(ByteBufCodecs.list()), MsgNewSpiralPatternsS2C::patterns,
            ByteBufCodecs.VAR_INT, MsgNewSpiralPatternsS2C::lifetime,
            MsgNewSpiralPatternsS2C::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle() {
        Handler.handle(this);
    }

    public static final class Handler {

        public static void handle(MsgNewSpiralPatternsS2C self) {
            Minecraft.getInstance().execute(() -> {
                var mc = Minecraft.getInstance();
                assert mc.level != null;
                var player = mc.level.getPlayerByUUID(self.playerUUID);
                var stack = IClientXplatAbstractions.INSTANCE.getClientCastingStack(player);

                for (var pattern : self.patterns)
                    stack.addPattern(pattern, self.lifetime);
            });
        }
    }
}
