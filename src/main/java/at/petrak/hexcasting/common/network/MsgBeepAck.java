package at.petrak.hexcasting.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
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
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    var world = Minecraft.getInstance().level;
                    if (world != null){
                        float pitch = (float) Math.pow(2, (note - 12) / 12.0);
                        world.playSound(null, target.x, target.y, target.z, instrument.getSoundEvent(), SoundSource.PLAYERS, 3, pitch);
                        world.addParticle(ParticleTypes.NOTE, target.x, target.y + 0.2, target.z, note / 24.0, 0, 0);
                    }
                })
        );
        ctx.get().setPacketHandled(true);
    }
}
