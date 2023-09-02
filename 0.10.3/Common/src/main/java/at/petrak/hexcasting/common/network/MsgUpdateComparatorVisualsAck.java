package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent server->client when a player is looking at a block through a lens whose comparator value is not the same as what they last saw.
 */
public record MsgUpdateComparatorVisualsAck(BlockPos pos, int comparator, int bee) implements IMessage {
    public static final ResourceLocation ID = modLoc("cmp");

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgUpdateComparatorVisualsAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        int comparator = buf.readInt();
        int bee = buf.readInt();
        BlockPos pos = comparator == -1 && bee == -1 ? null : buf.readBlockPos();

        return new MsgUpdateComparatorVisualsAck(pos, comparator, bee);
    }

    public void serialize(FriendlyByteBuf buf) {
        buf.writeInt(this.comparator);
        buf.writeInt(this.bee);
        if (this.comparator != -1 || this.bee != -1) {
            buf.writeBlockPos(this.pos);
        }
    }

    public static void handle(MsgUpdateComparatorVisualsAck msg) {
        Minecraft.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                ScryingLensOverlayRegistry.receiveComparatorAndBeeValue(msg.pos(), msg.comparator(), msg.bee());
            }
        });
    }

}
