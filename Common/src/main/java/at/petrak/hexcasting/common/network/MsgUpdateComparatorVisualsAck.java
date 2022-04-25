package at.petrak.hexcasting.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent server->client when a player is looking at a block through a lens whose comparator value is not the same as what they last saw.
 */
public record MsgUpdateComparatorVisualsAck(BlockPos pos, int value) {

    public static MsgUpdateComparatorVisualsAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        int value = buf.readInt();
        BlockPos pos = value == -1 ? null : buf.readBlockPos();

        return new MsgUpdateComparatorVisualsAck(pos, value);
    }

    public void serialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        buf.writeInt(this.value);
        if (this.value != -1) {
            buf.writeBlockPos(this.pos);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientPacketHandler handler = new ClientPacketHandler(this);
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> handler::updateComparator);
        });
        ctx.get().setPacketHandled(true);
    }

}
