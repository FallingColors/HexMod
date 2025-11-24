package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.common.entities.EntityWallScroll;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent S->C to have a wall scroll recalculate its pattern, to get readability offset.
 */
public record MsgRecalcWallScrollDisplayS2C(int entityId, boolean showStrokeOrder) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, MsgRecalcWallScrollDisplayS2C> CODEC = CustomPacketPayload.codec(MsgRecalcWallScrollDisplayS2C::serialize, MsgRecalcWallScrollDisplayS2C::deserialize);
    public static final Type<MsgRecalcWallScrollDisplayS2C> ID = new Type<>(modLoc("redoscroll"));

    public static MsgRecalcWallScrollDisplayS2C deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        var id = buf.readVarInt();
        var showStrokeOrder = buf.readBoolean();
        return new MsgRecalcWallScrollDisplayS2C(id, showStrokeOrder);
    }

    public void serialize(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeBoolean(showStrokeOrder);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static void handle(MsgRecalcWallScrollDisplayS2C msg) {
        Minecraft.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                var mc = Minecraft.getInstance();
                var entity = mc.level.getEntity(msg.entityId);
                if (entity instanceof EntityWallScroll scroll
                    && scroll.getShowsStrokeOrder() != msg.showStrokeOrder) {
                    scroll.recalculateDisplay();
                }
            }
        });
    }
}
