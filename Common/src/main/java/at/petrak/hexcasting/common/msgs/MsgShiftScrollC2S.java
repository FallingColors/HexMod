package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.eval.ResolvedPattern;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.items.storage.ItemAbacus;
import at.petrak.hexcasting.common.items.storage.ItemSpellbook;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.lib.HexSounds;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
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
public record MsgShiftScrollC2S(double mainHandDelta, double offHandDelta, boolean isCtrl, boolean invertSpellbook,
                                boolean invertAbacus) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MsgShiftScrollC2S> TYPE = new CustomPacketPayload.Type<>(HexAPI.modLoc("scroll"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgShiftScrollC2S> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, MsgShiftScrollC2S::mainHandDelta,
            ByteBufCodecs.DOUBLE, MsgShiftScrollC2S::offHandDelta,
            ByteBufCodecs.BOOL, MsgShiftScrollC2S::isCtrl,
            ByteBufCodecs.BOOL, MsgShiftScrollC2S::invertSpellbook,
            ByteBufCodecs.BOOL, MsgShiftScrollC2S::invertAbacus,
            MsgShiftScrollC2S::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
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
        if (hand == InteractionHand.OFF_HAND && stack.has(DataComponents.CUSTOM_NAME)) {
            if (sealed) {
                component = Component.translatable("hexcasting.tooltip.spellbook.page_with_name.sealed",
                    Component.literal(String.valueOf(newIdx)).withStyle(ChatFormatting.WHITE),
                    Component.literal(String.valueOf(len)).withStyle(ChatFormatting.WHITE),
                    Component.literal("").withStyle(stack.getRarity().getStyleModifier()).withStyle(ChatFormatting.ITALIC)
                        .append(stack.getHoverName()),
                    Component.translatable("hexcasting.tooltip.spellbook.sealed").withStyle(ChatFormatting.GOLD));
            } else {
                component = Component.translatable("hexcasting.tooltip.spellbook.page_with_name",
                    Component.literal(String.valueOf(newIdx)).withStyle(ChatFormatting.WHITE),
                    Component.literal(String.valueOf(len)).withStyle(ChatFormatting.WHITE),
                    Component.literal("").withStyle(stack.getRarity().getStyleModifier()).withStyle(ChatFormatting.ITALIC)
                        .append(stack.getHoverName()));
            }

        } else {
            if (sealed) {
                component = Component.translatable("hexcasting.tooltip.spellbook.page.sealed",
                    Component.literal(String.valueOf(newIdx)).withStyle(ChatFormatting.WHITE),
                    Component.literal(String.valueOf(len)).withStyle(ChatFormatting.WHITE),
                    Component.translatable("hexcasting.tooltip.spellbook.sealed").withStyle(ChatFormatting.GOLD));
            } else {
                component = Component.translatable("hexcasting.tooltip.spellbook.page",
                    Component.literal(String.valueOf(newIdx)).withStyle(ChatFormatting.WHITE),
                    Component.literal(String.valueOf(len)).withStyle(ChatFormatting.WHITE));
            }
        }

        sender.displayClientMessage(component.withStyle(ChatFormatting.GRAY), true);
    }

    private void abacus(ServerPlayer sender, InteractionHand hand, ItemStack stack, double delta) {
        if (invertAbacus) {
            delta = -delta;
        }

        var increase = delta < 0;
        Double num = stack.get(HexDataComponents.ABACUS_VALUE);
        if(num == null)
            num = 0.0;

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
        stack.set(HexDataComponents.ABACUS_VALUE, num);

        pitch *= (increase ? 1.05f : 0.95f);
        pitch += (Math.random() - 0.5) * 0.1;
        sender.level().playSound(null, sender.getX(), sender.getY(), sender.getZ(),
            HexSounds.ABACUS, SoundSource.PLAYERS, 0.5f, pitch);

        var datum = HexItems.ABACUS.readIota(stack);
        if (datum != null) {
            var popup = datum.display();
            sender.displayClientMessage(
                Component.translatable("hexcasting.tooltip.abacus", popup).withStyle(ChatFormatting.GREEN), true);
        }
    }
}
