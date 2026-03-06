package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.entities.EntityWallScroll;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

// https://github.com/VazkiiMods/Botania/blob/1.18.x/Xplat/src/main/java/vazkii/botania/network/clientbound/PacketSpawnDoppleganger.java
public record MsgNewWallScrollS2C(ClientboundAddEntityPacket inner, BlockPos pos, Direction dir, ItemStack scrollItem,
                                  boolean showsStrokeOrder, int blockSize) implements IMessage {
    public static final ResourceLocation ID = modLoc("wallscr");
    public static final CustomPacketPayload.Type<MsgNewWallScrollS2C> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, MsgNewWallScrollS2C> STREAM_CODEC =
        StreamCodec.ofMember(MsgNewWallScrollS2C::serialize, MsgNewWallScrollS2C::deserialize);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    @Override
    public void serialize(FriendlyByteBuf buf) {
        var rfb = (RegistryFriendlyByteBuf) buf;
        ClientboundAddEntityPacket.STREAM_CODEC.encode(rfb, inner);
        buf.writeBlockPos(pos);
        buf.writeByte(dir.ordinal());
        ItemStack.STREAM_CODEC.encode(rfb, scrollItem);
        buf.writeBoolean(showsStrokeOrder);
        buf.writeVarInt(blockSize);
    }

    public static MsgNewWallScrollS2C deserialize(FriendlyByteBuf buf) {
        var rfb = (RegistryFriendlyByteBuf) buf;
        var inner = ClientboundAddEntityPacket.STREAM_CODEC.decode(rfb);
        var pos = buf.readBlockPos();
        var dir = HexUtils.getSafe(Direction.values(), buf.readByte());
        var scroll = ItemStack.STREAM_CODEC.decode(rfb);
        var strokeOrder = buf.readBoolean();
        var blockSize = buf.readVarInt();
        return new MsgNewWallScrollS2C(inner, pos, dir, scroll, strokeOrder, blockSize);
    }

    public static void handle(MsgNewWallScrollS2C self) {
        Minecraft.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                var player = Minecraft.getInstance().player;
                if (player != null) {
                    player.connection.handleAddEntity(self.inner);
                    var e = player.level().getEntity(self.inner.getId());
                    if (e instanceof EntityWallScroll scroll) {
                        scroll.readSpawnData(self.pos, self.dir, self.scrollItem, self.showsStrokeOrder,
                            self.blockSize);
                    }
                }
            }
        });
    }
}
