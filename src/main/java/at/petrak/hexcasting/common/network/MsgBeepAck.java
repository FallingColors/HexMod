package at.petrak.hexcasting.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent server->client to synchronize OpAddMotion when the target is a player.
 */
public record MsgBeepAck(Vec3 target, int note, NoteBlockInstrument instrument) {
    public static MsgBeepAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        var x = buf.readDouble();
        var y = buf.readDouble();
        var z = buf.readDouble();
        var note = buf.readInt();
        var instrument = buf.readEnum(NoteBlockInstrument.class);
        return new MsgBeepAck(new Vec3(x, y, z), note, instrument);
    }

    public void serialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        buf.writeDouble(this.target.x);
        buf.writeDouble(this.target.y);
        buf.writeDouble(this.target.z);
        buf.writeInt(this.note);
        buf.writeEnum(instrument);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientPacketHandler handler = new ClientPacketHandler(this);
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> handler::beep);
        });
        ctx.get().setPacketHandled(true);
    }
}
