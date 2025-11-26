package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.entities.EntityWallScroll;
import at.petrak.paucal.api.PaucalCodecs;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

// https://github.com/VazkiiMods/Botania/blob/1.18.x/Xplat/src/main/java/vazkii/botania/network/clientbound/PacketSpawnDoppleganger.java
public record MsgNewWallScrollS2C(ClientboundAddEntityPacket inner, BlockPos pos, Direction dir, ItemStack scrollItem,
                                  boolean showsStrokeOrder, int blockSize) implements CustomPacketPayload {
<<<<<<< HEAD
    public static final StreamCodec<RegistryFriendlyByteBuf, MsgNewWallScrollS2C> CODEC = CustomPacketPayload.codec(MsgNewWallScrollS2C::serialize, MsgNewWallScrollS2C::deserialize);
    public static final Type<MsgNewWallScrollS2C> ID = new Type<>(modLoc("wallscr"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public void serialize(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(inner.getId());
        buf.writeUUID(inner.getUUID());
        ByteBufCodecs.registry(Registries.ENTITY_TYPE).encode(buf, inner.getType());
        buf.writeDouble(inner.getX());
        buf.writeDouble(inner.getY());
        buf.writeDouble(inner.getZ());
        buf.writeByte((byte) inner.getXRot());
        buf.writeByte((byte) inner.getYRot());
        buf.writeByte((byte) inner.getYHeadRot());
        buf.writeVarInt(inner.getData());
        buf.writeShort((short) inner.getXa());
        buf.writeShort((short) inner.getYa());
        buf.writeShort((short) inner.getZa());
        buf.writeBlockPos(pos);
        buf.writeByte(dir.ordinal());
        ItemStack.STREAM_CODEC.encode(buf, scrollItem);
        buf.writeBoolean(showsStrokeOrder);
        buf.writeVarInt(blockSize);
    }

    public static MsgNewWallScrollS2C deserialize(RegistryFriendlyByteBuf buf) {
        var inner = ClientboundAddEntityPacket.STREAM_CODEC.decode(buf);
        var pos = buf.readBlockPos();
        var dir = HexUtils.getSafe(Direction.values(), buf.readByte());
        var scroll = ItemStack.STREAM_CODEC.decode(buf);
        var strokeOrder = buf.readBoolean();
        var blockSize = buf.readVarInt();
        return new MsgNewWallScrollS2C(inner, pos, dir, scroll, strokeOrder, blockSize);
=======
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
>>>>>>> refs/remotes/slava/devel/port-1.21
    }

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
