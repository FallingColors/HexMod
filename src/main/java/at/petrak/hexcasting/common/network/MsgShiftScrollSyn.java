package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.common.items.HexItems;
import at.petrak.hexcasting.common.items.ItemAbacus;
import at.petrak.hexcasting.common.items.ItemSpellbook;
import at.petrak.hexcasting.common.lib.HexSounds;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent client->server when the client shift+scrolls with a shift-scrollable item
 * or scrolls in the spellcasting UI.
 */
public record MsgShiftScrollSyn(InteractionHand hand, double scrollDelta, boolean isCtrl) {
    public static MsgShiftScrollSyn deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        var hand = InteractionHand.values()[buf.readInt()];
        var scrollDelta = buf.readDouble();
        var isCtrl = buf.readBoolean();
        return new MsgShiftScrollSyn(hand, scrollDelta, isCtrl);
    }

    public void serialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        buf.writeInt(this.hand.ordinal());
        buf.writeDouble(this.scrollDelta);
        buf.writeBoolean(this.isCtrl);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                var stack = sender.getItemInHand(hand);

                if (stack.getItem() == HexItems.SPELLBOOK.get()) {
                    spellbook(sender, stack);
                } else if (stack.getItem() == HexItems.ABACUS.get()) {
                    abacus(sender, stack);
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
        sender.displayClientMessage(new TranslatableComponent("hexcasting.tooltip.spellbook.page", newIdx, len), true);
    }

    private void abacus(ServerPlayer sender, ItemStack stack) {
        var increase = this.scrollDelta < 0;
        var tag = stack.getTag();
        double num = 0d;
        if (tag != null) {
            num = tag.getDouble(ItemAbacus.TAG_VALUE);
        }

        double delta;
        float pitch;
        if (this.hand == InteractionHand.MAIN_HAND) {
            delta = this.isCtrl ? 10 : 1;
            pitch = this.isCtrl ? 0.7f : 0.9f;
        } else {
            delta = this.isCtrl ? 0.01 : 0.1;
            pitch = this.isCtrl ? 1.3f : 1.0f;
        }

        num += delta * (increase ? 1 : -1);
        stack.getOrCreateTag().putDouble(ItemAbacus.TAG_VALUE, num);

        pitch *= (increase ? 1.05f : 0.95f);
        pitch += (Math.random() - 0.5) * 0.1;
        sender.level.playSound(null, sender.getX(), sender.getY(), sender.getZ(),
            HexSounds.ABACUS.get(), SoundSource.PLAYERS, 0.5f, pitch);

        var popup = SpellDatum.DisplayFromTag(HexItems.ABACUS.get().readDatumTag(stack));
        sender.displayClientMessage(new TranslatableComponent("hexcasting.tooltip.abacus", popup), true);
    }
}
