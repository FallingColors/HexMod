package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.entities.EntityWallScroll;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
<<<<<<< HEAD
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
=======
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
>>>>>>> refs/remotes/slava/devel/port-1.21
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent S->C to have a wall scroll recalculate its pattern, to get readability offset.
 */
public record MsgRecalcWallScrollDisplayS2C(int entityId, boolean showStrokeOrder) implements CustomPacketPayload {
<<<<<<< HEAD
    public static final StreamCodec<FriendlyByteBuf, MsgRecalcWallScrollDisplayS2C> CODEC = CustomPacketPayload.codec(MsgRecalcWallScrollDisplayS2C::serialize, MsgRecalcWallScrollDisplayS2C::deserialize);
    public static final Type<MsgRecalcWallScrollDisplayS2C> ID = new Type<>(modLoc("redoscroll"));
=======
    public static final CustomPacketPayload.Type<MsgRecalcWallScrollDisplayS2C> TYPE = new CustomPacketPayload.Type<>(HexAPI.modLoc("redoscroll"));
>>>>>>> refs/remotes/slava/devel/port-1.21

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgRecalcWallScrollDisplayS2C> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MsgRecalcWallScrollDisplayS2C::entityId,
            ByteBufCodecs.BOOL, MsgRecalcWallScrollDisplayS2C::showStrokeOrder,
            MsgRecalcWallScrollDisplayS2C::new
    );

<<<<<<< HEAD
    public void serialize(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeBoolean(showStrokeOrder);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
=======
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
>>>>>>> refs/remotes/slava/devel/port-1.21
    }

    public static void handle(MsgRecalcWallScrollDisplayS2C msg) {
        Minecraft.getInstance().execute(() -> {
            var mc = Minecraft.getInstance();
            var entity = mc.level.getEntity(msg.entityId);
            if (entity instanceof EntityWallScroll scroll
                && scroll.getShowsStrokeOrder() != msg.showStrokeOrder) {
                scroll.recalculateDisplay();
            }
        });
    }
}
