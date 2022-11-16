package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.common.entities.EntityWallScroll;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent S->C to have a wall scroll recalculate its pattern, to get readability offset.
 */
public record MsgRecalcWallScrollDisplayAck(int entityId, boolean showStrokeOrder) implements IMessage {
    public static final ResourceLocation ID = modLoc("redoscroll");

    public static MsgRecalcWallScrollDisplayAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        var id = buf.readVarInt();
        var showStrokeOrder = buf.readBoolean();
        return new MsgRecalcWallScrollDisplayAck(id, showStrokeOrder);
    }

    @Override
    public void serialize(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeBoolean(showStrokeOrder);
    }

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static void handle(MsgRecalcWallScrollDisplayAck msg) {
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
