package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.common.items.ItemSpellbook;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent client->server when the client shift+scrolls with a shift-scrollable item
 * or scrolls in the spellcasting UI.
 */
public record MsgShiftScrollSyn(InteractionHand hand, double scrollDelta) {
    public static MsgShiftScrollSyn deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        var hand = InteractionHand.values()[buf.readInt()];
        var scrollDelta = buf.readDouble();
        return new MsgShiftScrollSyn(hand, scrollDelta);
    }

    public void serialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        buf.writeInt(this.hand.ordinal());
        buf.writeDouble(this.scrollDelta);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                var stack = sender.getItemInHand(hand);

                if (stack.getItem() instanceof ItemSpellbook) {
                    spellbook(sender, stack);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private void spellbook(ServerPlayer sender, ItemStack stack) {
        var tag = stack.getOrCreateTag();
        ItemSpellbook.RotatePageIdx(tag, this.scrollDelta < 0.0);

        var newIdx = tag.getInt(ItemSpellbook.TAG_SELECTED_PAGE);
        var len = ItemSpellbook.HighestPage(tag.getCompound(ItemSpellbook.TAG_PAGES));
        sender.displayClientMessage(new TranslatableComponent("hexcasting.spellbook.tooltip.page", newIdx, len), true);
    }
}
