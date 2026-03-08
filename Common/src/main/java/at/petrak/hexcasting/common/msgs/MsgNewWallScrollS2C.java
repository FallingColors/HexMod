package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.entities.EntityWallScroll;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.item.ItemStack;

// https://github.com/VazkiiMods/Botania/blob/1.18.x/Xplat/src/main/java/vazkii/botania/network/clientbound/PacketSpawnDoppleganger.java
public record MsgNewWallScrollS2C(ClientboundAddEntityPacket inner, BlockPos pos, Direction dir, ItemStack scrollItem,
                                  boolean showsStrokeOrder, int blockSize) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MsgNewWallScrollS2C> TYPE = new CustomPacketPayload.Type<>(HexAPI.modLoc("wallscr"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgNewWallScrollS2C> STREAM_CODEC = StreamCodec.composite(
            ClientboundAddEntityPacket.STREAM_CODEC, MsgNewWallScrollS2C::inner,
            BlockPos.STREAM_CODEC, MsgNewWallScrollS2C::pos,
            Direction.STREAM_CODEC, MsgNewWallScrollS2C::dir,
            ItemStack.STREAM_CODEC, MsgNewWallScrollS2C::scrollItem,
            ByteBufCodecs.BOOL, MsgNewWallScrollS2C::showsStrokeOrder,
            ByteBufCodecs.VAR_INT, MsgNewWallScrollS2C::blockSize,
            MsgNewWallScrollS2C::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle() {
        Handler.handle(this);
    }

    public static final class Handler {

        public static void handle(MsgNewWallScrollS2C self) {
            Minecraft.getInstance().execute(() -> {
                var player = Minecraft.getInstance().player;
                if (player != null) {
                    player.connection.handleAddEntity(self.inner);
                    var e = player.level().getEntity(self.inner.getId());
                    if (e instanceof EntityWallScroll scroll) {
                        scroll.readSpawnData(self.pos, self.dir, self.scrollItem, self.showsStrokeOrder,
                                self.blockSize);
                    }
                }
            });
        }
    }
}
