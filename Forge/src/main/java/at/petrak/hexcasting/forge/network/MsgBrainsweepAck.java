package at.petrak.hexcasting.forge.network;

import at.petrak.hexcasting.common.misc.Brainsweeping;
import at.petrak.hexcasting.common.network.IMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent server->client to synchronize the status of a brainswept mob.
 */
public record MsgBrainsweepAck(int target) implements IMessage {
    public static final ResourceLocation ID = modLoc("sweep");

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgBrainsweepAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        var target = buf.readInt();
        return new MsgBrainsweepAck(target);
    }

    @Override
    public void serialize(FriendlyByteBuf buf) {
        buf.writeInt(target);
    }

    public static MsgBrainsweepAck of(Entity target) {
        return new MsgBrainsweepAck(target.getId());
    }

    public static void handle(MsgBrainsweepAck msg) {
        Minecraft.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                var level = Minecraft.getInstance().level;
                if (level != null) {
                    Entity entity = level.getEntity(msg.target());
                    if (entity instanceof Mob living) {
                        Brainsweeping.brainsweep(living);
                    }
                }
            }
        });
    }
}
