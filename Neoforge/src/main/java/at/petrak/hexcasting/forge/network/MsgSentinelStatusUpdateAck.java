package at.petrak.hexcasting.forge.network;

import at.petrak.hexcasting.api.player.Sentinel;
import at.petrak.hexcasting.common.msgs.IMessage;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent server->client to synchronize the status of the sentinel.
 */
public record MsgSentinelStatusUpdateAck(@Nullable Sentinel update) implements IMessage {
    public static final ResourceLocation ID = modLoc("sntnl");

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgSentinelStatusUpdateAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        var exists = buf.readBoolean();
        if (!exists) {
            return new MsgSentinelStatusUpdateAck(null);
        }

        var greater = buf.readBoolean();
        var origin = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        var dimension = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());

        var sentinel = new Sentinel(greater, origin, dimension);
        return new MsgSentinelStatusUpdateAck(sentinel);
    }

    public void serialize(FriendlyByteBuf buf) {
        if (update == null) {
            buf.writeBoolean(false);
            return;
        }

        buf.writeBoolean(true);
        buf.writeBoolean(update.extendsRange());
        buf.writeDouble(update.position().x);
        buf.writeDouble(update.position().y);
        buf.writeDouble(update.position().z);
        buf.writeResourceLocation(update.dimension().location());
    }

    public static void handle(MsgSentinelStatusUpdateAck self) {
        //noinspection Convert2Lambda
        Minecraft.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                var player = Minecraft.getInstance().player;
                if (player != null) {
                    IXplatAbstractions.INSTANCE.setSentinel(player, self.update());
                }
            }
        });
    }
}
