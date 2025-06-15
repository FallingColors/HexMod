package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.paucal.api.PaucalCodecs;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.Vec3;

public record MsgBeepS2C(Vec3 target, int note, NoteBlockInstrument instrument) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MsgBeepS2C> TYPE = new CustomPacketPayload.Type<>(HexAPI.modLoc("beep"));

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
