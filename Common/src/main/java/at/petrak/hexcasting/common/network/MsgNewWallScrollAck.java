package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.common.entities.EntityWallScroll;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

// https://github.com/VazkiiMods/Botania/blob/1.18.x/Xplat/src/main/java/vazkii/botania/network/clientbound/PacketSpawnDoppleganger.java
public record MsgNewWallScrollAck(ClientboundAddEntityPacket inner, BlockPos pos, Direction dir, ItemStack scrollItem,
                                  boolean showsStrokeOrder, int blockSize) implements IMessage {
    public static final ResourceLocation ID = modLoc("wallscr");

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    @Override
    public void serialize(FriendlyByteBuf buf) {
        inner.write(buf);
        buf.writeBlockPos(pos);
        buf.writeByte(dir.ordinal());
        buf.writeItem(scrollItem);
        buf.writeBoolean(showsStrokeOrder);
        buf.writeVarInt(blockSize);
    }

    public static MsgNewWallScrollAck deserialize(FriendlyByteBuf buf) {
        var inner = new ClientboundAddEntityPacket(buf);
        var pos = buf.readBlockPos();
        var dir = Direction.values()[buf.readByte()];
        var scroll = buf.readItem();
        var strokeOrder = buf.readBoolean();
        var blockSize = buf.readVarInt();
        return new MsgNewWallScrollAck(inner, pos, dir, scroll, strokeOrder, blockSize);
    }

    public static void handle(MsgNewWallScrollAck self) {
        Minecraft.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                var player = Minecraft.getInstance().player;
                if (player != null) {
                    player.connection.handleAddEntity(self.inner);
                    var e = player.level.getEntity(self.inner.getId());
                    if (e instanceof EntityWallScroll scroll) {
                        scroll.readSpawnData(self.pos, self.dir, self.scrollItem, self.showsStrokeOrder,
                            self.blockSize);
                    }
                }
            }
        });
    }
}
