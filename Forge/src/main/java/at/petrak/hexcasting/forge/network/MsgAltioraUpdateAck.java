package at.petrak.hexcasting.forge.network;

import at.petrak.hexcasting.api.player.AltioraAbility;
import at.petrak.hexcasting.common.msgs.IMessage;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public record MsgAltioraUpdateAck(@Nullable AltioraAbility altiora) implements IMessage {
    public static final ResourceLocation ID = modLoc("altiora");

    @Override
    public ResourceLocation getFabricId() {
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
