package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.particles.ConjureParticleOptions;
import at.petrak.paucal.api.PaucalCodecs;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;

public record MsgSingleParticleS2C(Vec3 pos, FrozenPigment colorizer) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MsgSingleParticleS2C> TYPE = new CustomPacketPayload.Type<>(HexAPI.modLoc("prtcl_s"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgSingleParticleS2C> STREAM_CODEC = StreamCodec.composite(
            PaucalCodecs.VEC3, MsgSingleParticleS2C::pos,
            FrozenPigment.STREAM_CODEC, MsgSingleParticleS2C::colorizer,
            MsgSingleParticleS2C::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle() {
        MsgSingleParticleS2C.Handler.handle(this);
    }

    public static final class Handler {

        public static void handle(MsgSingleParticleS2C msg) {
            Minecraft.getInstance().execute(() -> {
                var level = Minecraft.getInstance().level;
                if (level == null) return;
                var colProvider = msg.colorizer().getColorProvider();

                var color = colProvider.getRandomColor(level.random);
                level.addParticle(new ConjureParticleOptions(color), msg.pos.x, msg.pos.y, msg.pos.z, 0.0, 0.0, 0.0);
                for (int i = 0; i <= 10; i++) {
                    color = colProvider.getRandomColor(level.random);
                    double offsetX = level.random.nextFloat() * 0.1 - 0.05;
                    double offsetY = level.random.nextFloat() * 0.1 - 0.05;
                    double offsetZ = level.random.nextFloat() * 0.1 - 0.05;
                    level.addParticle(new ConjureParticleOptions(color),
                            msg.pos.x + offsetX, msg.pos.y + offsetY, msg.pos.z + offsetZ, 0.0, 0.0, 0.0);
                }
            });
        }
    }
}
