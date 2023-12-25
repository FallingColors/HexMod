package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public record MsgClearSpiralPatternsS2C(UUID playerUUID) implements IMessage {
    public static final ResourceLocation ID = modLoc("clr_spi_pats_sc");

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgClearSpiralPatternsS2C deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

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
