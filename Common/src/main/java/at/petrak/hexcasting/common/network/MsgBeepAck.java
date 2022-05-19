package at.petrak.hexcasting.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.Vec3;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public record MsgBeepAck(Vec3 target, int note, NoteBlockInstrument instrument) implements IMessage {
    public static final ResourceLocation ID = modLoc("beep");

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgBeepAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        var x = buf.readDouble();
        var y = buf.readDouble();
        var z = buf.readDouble();
        var note = buf.readInt();
        var instrument = buf.readEnum(NoteBlockInstrument.class);
        return new MsgBeepAck(new Vec3(x, y, z), note, instrument);
    }

    @Override
    public void serialize(FriendlyByteBuf buf) {
        buf.writeDouble(this.target.x);
        buf.writeDouble(this.target.y);
        buf.writeDouble(this.target.z);
        buf.writeInt(this.note);
        buf.writeEnum(instrument);
    }
    
    public static void handle(MsgBeepAck msg) {
        Minecraft.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                var minecraft = Minecraft.getInstance();
                var world = minecraft.level;
                if (world != null) {
                    float pitch = (float) Math.pow(2, (msg.note() - 12) / 12.0);
                    world.playLocalSound(msg.target().x, msg.target().y, msg.target().z,
                        msg.instrument().getSoundEvent(), SoundSource.PLAYERS, 3, pitch, false);
                    world.addParticle(ParticleTypes.NOTE, msg.target().x, msg.target().y + 0.2, msg.target().z,
                        msg.note() / 24.0, 0, 0);
                }
            }
        });
    }
}
