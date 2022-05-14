package at.petrak.hexcasting.common.items;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class JewelerHammerHandler {
	@SubscribeEvent
	public static void jewelerHammerBreakSpeed(PlayerEvent.BreakSpeed evt) {
		ItemStack stack = evt.getPlayer().getMainHandItem();
		if (stack.is(HexItems.JEWELER_HAMMER.get())) {
			if (Block.isShapeFullBlock(evt.getState().getShape(evt.getPlayer().level, evt.getPos()))) {
				evt.setCanceled(true);
			}
		}
	}
}
