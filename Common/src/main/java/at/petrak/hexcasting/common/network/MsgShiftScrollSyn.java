package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.api.spell.LegacySpellDatum;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.items.ItemAbacus;
import at.petrak.hexcasting.common.items.ItemSpellbook;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.lib.HexSounds;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent client->server when the client shift+scrolls with a shift-scrollable item
 * or scrolls in the spellcasting UI.
 */
public record MsgShiftScrollSyn(InteractionHand hand, double scrollDelta, boolean isCtrl) implements IMessage {
    public static final ResourceLocation ID = modLoc("scroll");

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgShiftScrollSyn deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        var hand = buf.readEnum(InteractionHand.class);
        var scrollDelta = buf.readDouble();
        var isCtrl = buf.readBoolean();
        return new MsgShiftScrollSyn(hand, scrollDelta, isCtrl);
    }

    public void serialize(FriendlyByteBuf buf) {
        buf.writeEnum(this.hand);
        buf.writeDouble(this.scrollDelta);
        buf.writeBoolean(this.isCtrl);
    }

    public void handle(MinecraftServer server, ServerPlayer sender) {
        server.execute(() -> {
            var stack = sender.getItemInHand(hand);

            if (stack.getItem() == HexItems.SPELLBOOK) {
                spellbook(sender, stack);
            } else if (stack.getItem() == HexItems.ABACUS) {
                abacus(sender, stack);
            }
        });
    }

    private void spellbook(ServerPlayer sender, ItemStack stack) {
        var newIdx = ItemSpellbook.RotatePageIdx(stack, this.scrollDelta < 0.0);

        var len = ItemSpellbook.HighestPage(stack);

        var sealed = ItemSpellbook.IsSealed(stack);

        MutableComponent component;
        if (hand == InteractionHand.OFF_HAND && stack.hasCustomHoverName()) {
            if (sealed) {
                component = new TranslatableComponent("hexcasting.tooltip.spellbook.page_with_name.sealed",
                    new TextComponent(String.valueOf(newIdx)).withStyle(ChatFormatting.WHITE),
                    new TextComponent(String.valueOf(len)).withStyle(ChatFormatting.WHITE),
                    new TextComponent("").withStyle(stack.getRarity().color, ChatFormatting.ITALIC)
                        .append(stack.getHoverName()),
                    new TranslatableComponent("hexcasting.tooltip.spellbook.sealed").withStyle(ChatFormatting.GOLD));
            } else {
                component = new TranslatableComponent("hexcasting.tooltip.spellbook.page_with_name",
                    new TextComponent(String.valueOf(newIdx)).withStyle(ChatFormatting.WHITE),
                    new TextComponent(String.valueOf(len)).withStyle(ChatFormatting.WHITE),
                    new TextComponent("").withStyle(stack.getRarity().color, ChatFormatting.ITALIC)
                        .append(stack.getHoverName()));
            }

        } else {
            if (sealed) {
                component = new TranslatableComponent("hexcasting.tooltip.spellbook.page.sealed",
                    new TextComponent(String.valueOf(newIdx)).withStyle(ChatFormatting.WHITE),
                    new TextComponent(String.valueOf(len)).withStyle(ChatFormatting.WHITE),
                    new TranslatableComponent("hexcasting.tooltip.spellbook.sealed").withStyle(ChatFormatting.GOLD));
            } else {
                component = new TranslatableComponent("hexcasting.tooltip.spellbook.page",
                    new TextComponent(String.valueOf(newIdx)).withStyle(ChatFormatting.WHITE),
                    new TextComponent(String.valueOf(len)).withStyle(ChatFormatting.WHITE));
            }
        }

        sender.displayClientMessage(component.withStyle(ChatFormatting.GRAY), true);
    }

    private void abacus(ServerPlayer sender, ItemStack stack) {
        var increase = this.scrollDelta < 0;
        double num = NBTHelper.getDouble(stack, ItemAbacus.TAG_VALUE);

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
        NBTHelper.putDouble(stack, ItemAbacus.TAG_VALUE, num);

        pitch *= (increase ? 1.05f : 0.95f);
        pitch += (Math.random() - 0.5) * 0.1;
        sender.level.playSound(null, sender.getX(), sender.getY(), sender.getZ(),
            HexSounds.ABACUS, SoundSource.PLAYERS, 0.5f, pitch);

        var datumTag = HexItems.ABACUS.readDatumTag(stack);
        if (datumTag != null) {
            var popup = LegacySpellDatum.displayFromNBT(datumTag);
            sender.displayClientMessage(
                new TranslatableComponent("hexcasting.tooltip.abacus", popup).withStyle(ChatFormatting.GREEN), true);
        }
    }
}
