package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.api.spell.ParticleSpray;
import at.petrak.hexcasting.api.misc.FrozenColorizer;
import at.petrak.hexcasting.common.particles.ConjureParticleOptions;
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
        var count = buf.readInt();
        var tag = buf.readAnySizeNbt();
        var colorizer = FrozenColorizer.deserialize(tag);
        return new MsgCastParticleAck(
            new ParticleSpray(new Vec3(posX, posY, posZ), new Vec3(velX, velY, velZ), fuzziness, spread, count),
            colorizer);
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
        buf.writeInt(this.spray.getCount());
        buf.writeNbt(this.colorizer.serialize());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                for (int i = 0; i < spray.getCount(); i++) {
                    // For the colors, pick any random time to get a mix of colors
                    var color = colorizer.getColor(RANDOM.nextFloat() * 256f, Vec3.ZERO);

                    var offset = randomInCircle(Mth.TWO_PI).normalize()
                        .scale(RANDOM.nextFloat() * spray.getSpread() / 2);
                    var pos = spray.getPos().add(offset);

                    var phi = Math.acos(1.0 - RANDOM.nextDouble() * (1.0 - Math.cos(spray.getSpread())));
                    var theta = Math.PI * 2.0 * RANDOM.nextDouble();
                    var v = spray.getVel().normalize();
                    // pick any old vector to get a vector normal to v with
                    Vec3 k;
                    if (v.x == 0.0 && v.y == 0.0) {
                        // oops, pick a *different* normal
                        k = new Vec3(1.0, 0.0, 0.0);
                    } else {
                        k = v.cross(new Vec3(0.0, 0.0, 1.0));
                    }
                    var velUnlen = v.scale(Math.cos(phi))
                        .add(k.scale(Math.sin(phi) * Math.cos(theta)))
                        .add(v.cross(k).scale(Math.sin(phi) * Math.sin(theta)));
                    var vel = velUnlen.scale(spray.getVel().length() / 20);

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
