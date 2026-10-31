package at.petrak.hexcasting.client;

import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.common.items.storage.ItemSpellbook;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import at.petrak.hexcasting.common.lib.HexItemComponents;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.msgs.MsgShiftScrollC2S;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

public class ShiftScrollListener {
    private static double mainHandDelta = 0;
    private static double offHandDelta = 0;

    public static boolean onScrollInGameplay(double delta) {
        if (Minecraft.getInstance().screen != null) {
            return false;
        }

        if (HexConfig.client().disableInworldScrolling()) return false;

        return onScroll(delta, true);
    }

    public static boolean onScroll(double delta, boolean needsSneaking) {
        return onScroll(delta, needsSneaking, true);
    }

    public static boolean onScroll(double delta, boolean needsSneaking, boolean allowInverting) {
        LocalPlayer player = Minecraft.getInstance().player;
        // not .isCrouching! that fails for players who are not on the ground
        // yes, this does work if you remap your sneak key
        if (player != null && (player.isShiftKeyDown() || !needsSneaking)) {
            // Spectators shouldn't interact with items!
            if (player.isSpectator()) {
                return false;
            }

            if (isScrollableStack(player.getMainHandItem())) {
                mainHandDelta += delta * getScrollModifier(player.getMainHandItem(), allowInverting);
                return true;
            } else if (isScrollableStack(player.getOffhandItem())) {
                offHandDelta += delta * getScrollModifier(player.getOffhandItem(), allowInverting);
                return true;
            }
        }

        return false;
    }

    public static void clientTickEnd() {
        if (mainHandDelta != 0 || offHandDelta != 0) {
            IClientXplatAbstractions.INSTANCE.sendPacketToServer(
                new MsgShiftScrollC2S(mainHandDelta, offHandDelta, Minecraft.getInstance().options.keySprint.isDown())
            );
            mainHandDelta = 0;
            offHandDelta = 0;
        }
    }

    private static boolean isScrollableStack(ItemStack stack) {
        return stack.has(HexDataComponents.SELECTED_SPELLBOOK_PAGE.get()) || stack.getItem() == HexItems.ABACUS;
    }

    private static double getScrollModifier(ItemStack stack, boolean allowInverting) {
        if (!allowInverting) return 1;

        if (stack.getItem() == HexItems.ABACUS) {
            return HexConfig.client().invertAbacusScrollDirection() ? -1 : 1;
        } else if (stack.has(HexDataComponents.SELECTED_SPELLBOOK_PAGE.get())) {
            return HexConfig.client().invertSpellbookScrollDirection() ? -1 : 1;
        } else {
            return 1;
        }
    }
}
