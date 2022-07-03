package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.items.ItemAbacus;
import at.petrak.hexcasting.common.items.ItemSpellbook;
import at.petrak.hexcasting.common.lib.HexIotaTypes;
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
public record MsgShiftScrollSyn(double mainHandDelta, double offHandDelta, boolean isCtrl, boolean invertSpellbook, boolean invertAbacus) implements IMessage {
    public static final ResourceLocation ID = modLoc("scroll");

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgShiftScrollSyn deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        var mainHandDelta = buf.readDouble();
        var offHandDelta = buf.readDouble();
        var isCtrl = buf.readBoolean();
        var invertSpellbook = buf.readBoolean();
        var invertAbacus = buf.readBoolean();
        return new MsgShiftScrollSyn(mainHandDelta, offHandDelta, isCtrl, invertSpellbook, invertAbacus);
    }

    public void serialize(FriendlyByteBuf buf) {
        buf.writeDouble(this.mainHandDelta);
        buf.writeDouble(this.offHandDelta);
        buf.writeBoolean(this.isCtrl);
        buf.writeBoolean(this.invertSpellbook);
        buf.writeBoolean(this.invertAbacus);
    }

    public void handle(MinecraftServer server, ServerPlayer sender) {
        server.execute(() -> {
            handleForHand(sender, InteractionHand.MAIN_HAND, mainHandDelta);
            handleForHand(sender, InteractionHand.OFF_HAND, offHandDelta);
        });
    }

    private void handleForHand(ServerPlayer sender, InteractionHand hand, double delta) {
        if (delta != 0) {
            var stack = sender.getItemInHand(hand);

            if (stack.getItem() == HexItems.SPELLBOOK) {
                spellbook(sender, hand, stack, delta);
            } else if (stack.getItem() == HexItems.ABACUS) {
                abacus(sender, hand, stack, delta);
            }
        }
    }

    private void spellbook(ServerPlayer sender, InteractionHand hand, ItemStack stack, double delta) {
        if (invertSpellbook) {
            delta = -delta;
        }

        var newIdx = ItemSpellbook.rotatePageIdx(stack, delta < 0.0);

        var len = ItemSpellbook.highestPage(stack);

        var sealed = ItemSpellbook.isSealed(stack);

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

    private void abacus(ServerPlayer sender, InteractionHand hand, ItemStack stack, double delta) {
        if (invertAbacus) {
            delta = -delta;
        }

        var increase = delta < 0;
        double num = NBTHelper.getDouble(stack, ItemAbacus.TAG_VALUE);

        double shiftDelta;
        float pitch;
        if (hand == InteractionHand.MAIN_HAND) {
            shiftDelta = this.isCtrl ? 10 : 1;
            pitch = this.isCtrl ? 0.7f : 0.9f;
        } else {
            shiftDelta = this.isCtrl ? 0.01 : 0.1;
            pitch = this.isCtrl ? 1.3f : 1.0f;
        }

        int scale = Math.max((int) Math.floor(Math.abs(delta)), 1);

        num += scale * shiftDelta * (increase ? 1 : -1);
        NBTHelper.putDouble(stack, ItemAbacus.TAG_VALUE, num);

        pitch *= (increase ? 1.05f : 0.95f);
        pitch += (Math.random() - 0.5) * 0.1;
        sender.level.playSound(null, sender.getX(), sender.getY(), sender.getZ(),
            HexSounds.ABACUS, SoundSource.PLAYERS, 0.5f, pitch);

        var datumTag = HexItems.ABACUS.readIotaTag(stack);
        if (datumTag != null) {
            var popup = HexIotaTypes.getDisplay(datumTag);
            sender.displayClientMessage(
                new TranslatableComponent("hexcasting.tooltip.abacus", popup).withStyle(ChatFormatting.GREEN), true);
        }
    }
}
