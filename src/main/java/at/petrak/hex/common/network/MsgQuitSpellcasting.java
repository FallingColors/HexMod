package at.petrak.hex.common.network;

import at.petrak.hex.common.items.ItemWand;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent client->server when the client exits spellcasting early.
 */
public record MsgQuitSpellcasting() {
    public static MsgQuitSpellcasting deserialize(ByteBuf _buf) {
        return new MsgQuitSpellcasting();
    }

    public void serialize(ByteBuf _buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                var held = sender.getMainHandItem();
                if (held.getItem() instanceof ItemWand) {
                    // Todo: appropriate consequences for quitting a spell like this
                    held.getOrCreateTag().put(ItemWand.TAG_HARNESS, new CompoundTag());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
