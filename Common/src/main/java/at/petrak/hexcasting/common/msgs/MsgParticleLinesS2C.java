package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.particles.ConjureParticleOptions;
import at.petrak.paucal.api.PaucalCodecs;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record MsgParticleLinesS2C(List<Vec3> locs, FrozenPigment colorizer) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MsgParticleLinesS2C> TYPE = new CustomPacketPayload.Type<>(HexAPI.modLoc("prtcl_l"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgParticleLinesS2C> STREAM_CODEC = StreamCodec.composite(
            PaucalCodecs.VEC3.apply(ByteBufCodecs.list()), MsgParticleLinesS2C::locs,
            FrozenPigment.STREAM_CODEC, MsgParticleLinesS2C::colorizer,
            MsgParticleLinesS2C::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle() {
        MsgParticleLinesS2C.Handler.handle(this);
    }

    public static final class Handler {

        public static void handle(MsgParticleLinesS2C msg) {
            Minecraft.getInstance().execute(() -> {
                var level = Minecraft.getInstance().level;
                if (level == null) return;
                var colProvider = msg.colorizer().getColorProvider();

                for (int i = 0; i < msg.locs.size()-1; ++i) {
                    Vec3 start = msg.locs.get(i);
                    Vec3 end = msg.locs.get(i+1);
                    int steps = (int) (end.subtract(start).length() * 10);
                    for (int j = 0; j <= steps; ++j) {
                        Vec3 pos = start.add(end.subtract(start).scale((double) j / steps));
                        var color = colProvider.getRandomColor(level.random);
                        level.addParticle(new ConjureParticleOptions(color),
                                pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
                    }
                }
            });
        }
    }
}
