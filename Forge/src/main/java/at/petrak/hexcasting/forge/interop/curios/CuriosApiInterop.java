package at.petrak.hexcasting.forge.interop.curios;

import at.petrak.hexcasting.common.items.ItemLens;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.interop.HexInterop;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.concurrent.atomic.AtomicBoolean;

public class CuriosApiInterop {

	public static void init() {
		ItemLens.addLensHUDPredicate(player -> {
			AtomicBoolean hasLens = new AtomicBoolean(false);
			player.getCapability(CuriosCapability.INVENTORY).ifPresent(handler -> {
				ICurioStacksHandler stacksHandler = handler.getCurios().get("head");
				if(stacksHandler != null) hasLens.set(stacksHandler.getStacks().getStackInSlot(0).is(HexItems.SCRYING_LENS));
			});
			return hasLens.get();
		});
	}

	public static void onInterModEnqueue(final InterModEnqueueEvent event) {
		InterModComms.sendTo(HexInterop.Forge.CURIOS_API_ID, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.HEAD.getMessageBuilder().build());
	}

	public static void onClientSetup(final FMLClientSetupEvent event) {
		CuriosRenderers.register();
	}
}
