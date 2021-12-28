package at.petrak.hex.client;

import at.petrak.hex.HexMod;
import at.petrak.hex.common.items.ItemSpellbook;
import at.petrak.hex.common.network.HexMessages;
import at.petrak.hex.common.network.MsgShiftScrollSyn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HexMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ShiftScrollListener {
    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollEvent evt) {
        HexMod.LOGGER.info("scrolling {}", evt.getScrollDelta());
        LocalPlayer player = Minecraft.getInstance().player;
        if (player.isCrouching()) {
            InteractionHand hand = null;
            if (player.getMainHandItem().getItem() != Items.AIR) {
                hand = InteractionHand.MAIN_HAND;
            } else if (player.getOffhandItem().getItem() != Items.AIR) {
                hand = InteractionHand.OFF_HAND;
            }

            if (hand != null) {
                var item = player.getItemInHand(hand).getItem();
                if (item instanceof ItemSpellbook) {
                    evt.setCanceled(true);

                    HexMessages.getNetwork().sendToServer(new MsgShiftScrollSyn(hand, evt.getScrollDelta()));
                }
            }
        }

    }
}
