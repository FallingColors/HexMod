package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.paucal.api.PaucalCodecs;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
<<<<<<< HEAD
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
=======
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
>>>>>>> refs/remotes/slava/devel/port-1.21
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.Vec3;

public record MsgBeepS2C(Vec3 target, int note, NoteBlockInstrument instrument) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MsgBeepS2C> TYPE = new CustomPacketPayload.Type<>(HexAPI.modLoc("beep"));

<<<<<<< HEAD
public record MsgBeepS2C(Vec3 target, int note, NoteBlockInstrument instrument) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, MsgBeepS2C> CODEC = CustomPacketPayload.codec(MsgBeepS2C::serialize, MsgBeepS2C::deserialize);

    public static final Type<MsgBeepS2C> ID = new Type<>(modLoc("beep"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    private static void encode(FriendlyByteBuf buf, MsgBeepS2C msg) {
        msg.serialize(buf);
    }

    public static MsgBeepS2C deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        var x = buf.readDouble();
        var y = buf.readDouble();
        var z = buf.readDouble();
        var note = buf.readInt();
        var instrument = buf.readEnum(NoteBlockInstrument.class);
        return new MsgBeepS2C(new Vec3(x, y, z), note, instrument);
    }


    public void serialize(FriendlyByteBuf buf) {
        buf.writeDouble(this.target.x);
        buf.writeDouble(this.target.y);
        buf.writeDouble(this.target.z);
        buf.writeInt(this.note);
        buf.writeEnum(instrument);
=======
    public static final StreamCodec<RegistryFriendlyByteBuf, MsgBeepS2C> STREAM_CODEC = StreamCodec.composite(
            PaucalCodecs.VEC3, MsgBeepS2C::target,
            ByteBufCodecs.VAR_INT, MsgBeepS2C::note,
            ByteBufCodecs.idMapper(
                    (num) -> NoteBlockInstrument.values()[num],
                    NoteBlockInstrument::ordinal
            ), MsgBeepS2C::instrument,
            MsgBeepS2C::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
>>>>>>> refs/remotes/slava/devel/port-1.21
    }

    public static void handle(MsgBeepS2C msg) {
        Minecraft.getInstance().execute(() -> {
            var minecraft = Minecraft.getInstance();
            var world = minecraft.level;
            if (world != null) {
                float pitch = (float) Math.pow(2, (msg.note() - 12) / 12.0);
                world.playLocalSound(msg.target().x, msg.target().y, msg.target().z,
                    msg.instrument().getSoundEvent().value(), SoundSource.PLAYERS, 3, pitch, false);
                world.addParticle(ParticleTypes.NOTE, msg.target().x, msg.target().y + 0.2, msg.target().z,
                    msg.note() / 24.0, 0, 0);
            }
        });
    }


}
