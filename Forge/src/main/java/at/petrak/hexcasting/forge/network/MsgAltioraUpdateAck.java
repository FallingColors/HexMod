package at.petrak.hexcasting.forge.network;

import at.petrak.hexcasting.api.player.AltioraAbility;
import at.petrak.hexcasting.common.msgs.IMessage;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public record MsgAltioraUpdateAck(int playerId, @Nullable AltioraAbility altiora) implements IMessage {
    public static final ResourceLocation ID = modLoc("altiora");

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgAltioraUpdateAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        var playerId = buf.readInt();
        var extant = buf.readBoolean();
        if (!extant) {
            return new MsgAltioraUpdateAck(playerId, null);
        }
        var grace = buf.readVarInt();
        return new MsgAltioraUpdateAck(playerId, new AltioraAbility(grace));
    }

    @Override
    public void serialize(FriendlyByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeBoolean(this.altiora != null);
        if (this.altiora != null) {
            buf.writeVarInt(this.altiora.gracePeriod());
        }
    }

    public static void handle(MsgAltioraUpdateAck self) {
        Minecraft.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                if (Minecraft.getInstance().level == null) return;

                var entity = Minecraft.getInstance().level.getEntity(self.playerId());
                if (entity instanceof Player player) {
                    IXplatAbstractions.INSTANCE.setAltiora(player, self.altiora);
                }
            }
        });
    }
}
