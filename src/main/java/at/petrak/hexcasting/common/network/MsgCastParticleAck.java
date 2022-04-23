package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.api.misc.FrozenColorizer;
import at.petrak.hexcasting.api.spell.ParticleSpray;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent server->client to spray particles everywhere.
 */
public record MsgCastParticleAck(ParticleSpray spray, FrozenColorizer colorizer) {

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
        ctx.get().enqueueWork(() -> {
            ClientPacketHandler handler = new ClientPacketHandler(this);
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> handler::particleSpray);
        });
        ctx.get().setPacketHandled(true);
    }



}
