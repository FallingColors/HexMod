package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public record MsgNewSpiralPatternsS2C(UUID playerUUID, List<HexPattern> patterns, int lifetime) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, MsgNewSpiralPatternsS2C> CODEC = CustomPacketPayload.codec(MsgNewSpiralPatternsS2C::serialize, MsgNewSpiralPatternsS2C::deserialize);
    public static final Type<MsgNewSpiralPatternsS2C> ID = new Type<>(modLoc("spi_pats_sc"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static MsgNewSpiralPatternsS2C deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        var player = buf.readUUID();
        var patterns = buf.readCollection(ArrayList::new, buff -> HexPattern.fromNBT(buf.readNbt()));
        var lifetime = buf.readInt();


        return new MsgNewSpiralPatternsS2C(player, patterns, lifetime);
    }

    public void serialize(FriendlyByteBuf buf) {
        buf.writeUUID(playerUUID);
        buf.writeCollection(patterns, (buff, pattern) -> buff.writeNbt(pattern.serializeToNBT()));
        buf.writeInt(lifetime);
    }

    public static void handle(MsgNewSpiralPatternsS2C self) {
        Minecraft.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                var mc = Minecraft.getInstance();
                assert mc.level != null;
                var player = mc.level.getPlayerByUUID(self.playerUUID);
                var stack = IClientXplatAbstractions.INSTANCE.getClientCastingStack(player);

                for (var pattern : self.patterns)
                    stack.addPattern(pattern, self.lifetime);
            }
        });
    }
}
