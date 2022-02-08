package at.petrak.hexcasting.client;

import at.petrak.hexcasting.common.items.ItemSpellbook;
import at.petrak.hexcasting.common.network.HexMessages;
import at.petrak.hexcasting.common.network.MsgShiftScrollSyn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ShiftScrollListener {
    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollEvent evt) {
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
                evt.setCanceled(true);
                HexMessages.getNetwork().sendToServer(new MsgShiftScrollSyn(hand, evt.getScrollDelta()));
            }
        }
    }

    private static boolean IsScrollableItem(Item item) {
        return item instanceof ItemSpellbook;
    }
}
