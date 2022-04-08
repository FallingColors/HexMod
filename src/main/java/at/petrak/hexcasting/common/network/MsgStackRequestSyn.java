package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.common.casting.CastingContext;
import at.petrak.hexcasting.common.casting.CastingHarness;
import at.petrak.hexcasting.common.items.ItemWand;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * Sent client->server when the player opens the spell GUI.
 * Server will send back a MsgStackRequestAck packet
 */
public record MsgStackRequestSyn(InteractionHand handUsed) {
    public static MsgStackRequestSyn deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        var hand = InteractionHand.values()[buf.readInt()];
        return new MsgStackRequestSyn(hand);
    }

    public void serialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        buf.writeInt(this.handUsed.ordinal());
    }

    public void handle(Supplier<NetworkEvent.Context> networkCtx) {
        networkCtx.get().enqueueWork(() -> {
            ServerPlayer sender = networkCtx.get().getSender();
            if (sender != null) {
                var held = sender.getItemInHand(this.handUsed);
                if (held.getItem() instanceof ItemWand) {
                    var ctx = new CastingContext(sender, this.handUsed);
                    var tag = held.getOrCreateTag();
                    var harness = CastingHarness.DeserializeFromNBT(tag.getCompound(ItemWand.TAG_HARNESS), ctx);

                    HexMessages.getNetwork()
                        .send(PacketDistributor.PLAYER.with(() -> sender), new MsgStackRequestAck(harness.generateDescs()));
                }
            }
        });
        networkCtx.get().setPacketHandled(true);
    }

}
