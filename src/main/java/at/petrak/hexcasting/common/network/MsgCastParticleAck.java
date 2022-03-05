package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.api.ParticleSpray;
import at.petrak.hexcasting.common.casting.colors.FrozenColorizer;
import at.petrak.hexcasting.common.particles.ConjureParticleOptions;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.Random;
import java.util.function.Supplier;

/**
 * Sent server->client to spray particles everywhere.
 */
public record MsgCastParticleAck(ParticleSpray spray, FrozenColorizer colorizer) {
    private static final Random RANDOM = new Random();

    public static MsgCastParticleAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        var posX = buf.readDouble();
        var posY = buf.readDouble();
        var posZ = buf.readDouble();
        var velX = buf.readDouble();
        var velY = buf.readDouble();
        var velZ = buf.readDouble();
        var fuzziness = buf.readDouble();
        var spread = buf.readDouble();
        var tag = buf.readAnySizeNbt();
        var colorizer = FrozenColorizer.deserialize(tag);
        return new MsgCastParticleAck(
            new ParticleSpray(new Vec3(posX, posY, posZ), new Vec3(velX, velY, velZ), fuzziness, spread), colorizer);
    }

    public void serialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        buf.writeDouble(this.spray.getPos().x);
        buf.writeDouble(this.spray.getPos().y);
        buf.writeDouble(this.spray.getPos().z);
        buf.writeDouble(this.spray.getVel().x);
        buf.writeDouble(this.spray.getVel().y);
        buf.writeDouble(this.spray.getVel().z);
        buf.writeDouble(this.spray.getFuzziness());
        buf.writeDouble(this.spray.getSpread());
        buf.writeNbt(this.colorizer.serialize());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                for (int i = 0; i < 20; i++) {
                    // For the colors, pick any random time to get a mix of colors
                    var color = colorizer.getColor(RANDOM.nextFloat() * 256f, Vec3.ZERO);

                    var offset = randomInCircle(Mth.TWO_PI).scale(spray.getSpread());
                    var pos = spray.getPos().add(offset);

                    // https://math.stackexchange.com/questions/56784/generate-a-random-direction-within-a-cone
                    var northCone = randomInCircle(spray.getSpread());
                    var velNorm = spray.getVel().normalize();
                    var zp = new Vec3(0.0, 0.0, 1.0);
                    var rotAxis = velNorm.cross(zp);
                    var th = Math.acos(velNorm.dot(zp));
                    var dagn = new Quaternion(new Vector3f(rotAxis), (float) th, false);
                    var velf = new Vector3f(northCone);
                    velf.transform(dagn);
                    var vel = new Vec3(velf).scale(spray.getVel().length());

                    Minecraft.getInstance().level.addParticle(
                        new ConjureParticleOptions(color, false),
                        pos.x, pos.y, pos.z,
                        vel.x, vel.y, vel.z
                    );
                }
            })
        );
        ctx.get().setPacketHandled(true);
    }


    // https://math.stackexchange.com/questions/44689/how-to-find-a-random-axis-or-unit-vector-in-3d
    private static Vec3 randomInCircle(double maxTh) {
        var th = RANDOM.nextDouble(0.0, maxTh + 0.001);
        var z = RANDOM.nextDouble(-1.0, 1.0);
        return new Vec3(Math.sqrt(1.0 - z * z) * Math.cos(th), Math.sqrt(1.0 - z * z) * Math.sin(th), z);
    }
}
