package at.petrak.hexcasting.client;

import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.network.MsgShiftScrollSyn;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;

public class ShiftScrollListener {
    public static boolean onScroll(double delta) {
        LocalPlayer player = Minecraft.getInstance().player;
        // not .isCrouching! that fails for players who are not on the ground
        // yes, this does work if you remap your sneak key
        if (player.isShiftKeyDown()) {
            InteractionHand hand = null;
            if (IsScrollableItem(player.getMainHandItem().getItem())) {
                hand = InteractionHand.MAIN_HAND;
            } else if (IsScrollableItem(player.getOffhandItem().getItem())) {
                hand = InteractionHand.OFF_HAND;
            }

            if (hand != null) {
                IClientXplatAbstractions.INSTANCE.sendPacketToServer(
                    new MsgShiftScrollSyn(hand, delta, Screen.hasControlDown()));
                return true;
            }
        }

        return false;
    }

    private static boolean IsScrollableItem(Item item) {
        return item == HexItems.SPELLBOOK || item == HexItems.ABACUS;
    }
}
