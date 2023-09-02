package at.petrak.hexcasting.forge.network;

import at.petrak.hexcasting.api.player.Sentinel;
import at.petrak.hexcasting.common.network.IMessage;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent server->client to synchronize the status of the sentinel.
 */
public record MsgSentinelStatusUpdateAck(Sentinel update) implements IMessage {
    public static final ResourceLocation ID = modLoc("sntnl");

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgSentinelStatusUpdateAck deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);

        var exists = buf.readBoolean();
        var greater = buf.readBoolean();
        var origin = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        var dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation());

        var sentinel = new Sentinel(exists, greater, origin, dimension);
        return new MsgSentinelStatusUpdateAck(sentinel);
    }

    public void serialize(FriendlyByteBuf buf) {
        buf.writeBoolean(update.hasSentinel());
        buf.writeBoolean(update.extendsRange());
        buf.writeDouble(update.position().x);
        buf.writeDouble(update.position().y);
        buf.writeDouble(update.position().z);
        buf.writeResourceLocation(update.dimension().location());
    }

    public static void handle(MsgSentinelStatusUpdateAck self) {
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
