package at.petrak.hexcasting.common.msgs;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.Vec3;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public record MsgBeepS2C(Vec3 target, int note, NoteBlockInstrument instrument) implements IMessage {
    public static final ResourceLocation ID = modLoc("beep");
    public static final CustomPacketPayload.Type<MsgBeepS2C> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, MsgBeepS2C> STREAM_CODEC =
        StreamCodec.ofMember(MsgBeepS2C::serialize, MsgBeepS2C::deserialize);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgBeepS2C deserialize(FriendlyByteBuf buf) {
        var x = buf.readDouble();
        var y = buf.readDouble();
        var z = buf.readDouble();
        var note = buf.readInt();
        var instrument = buf.readEnum(NoteBlockInstrument.class);
        return new MsgBeepS2C(new Vec3(x, y, z), note, instrument);
    }

    @Override
    public void serialize(FriendlyByteBuf buf) {
        buf.writeDouble(this.target.x);
        buf.writeDouble(this.target.y);
        buf.writeDouble(this.target.z);
        buf.writeInt(this.note);
        buf.writeEnum(instrument);
    }

    public static void handle(MsgBeepS2C msg) {
        Minecraft.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                var minecraft = Minecraft.getInstance();
                var world = minecraft.level;
                if (world != null) {
                    float pitch = (float) Math.pow(2, (msg.note() - 12) / 12.0);
                    world.playLocalSound(msg.target().x, msg.target().y, msg.target().z,
                        msg.instrument().getSoundEvent().value(), SoundSource.PLAYERS, 3, pitch, false);
                    world.addParticle(ParticleTypes.NOTE, msg.target().x, msg.target().y + 0.2, msg.target().z,
                        msg.note() / 24.0, 0, 0);
                }
            }
        });
    }
}
