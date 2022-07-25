package at.petrak.hexcasting.fabric.interop.trinkets;

import at.petrak.hexcasting.common.items.ItemLens;
import at.petrak.hexcasting.common.lib.HexItems;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Optional;

public class TrinketsApiInterop {
	public static void init() {
		ItemLens.addLensHUDPredicate(player -> {
			Optional<TrinketComponent> optional = TrinketsApi.getTrinketComponent(player);
			if (optional.isPresent()) {
				TrinketComponent component = optional.get();
				return component.isEquipped(HexItems.SCRYING_LENS);
			}
			return false;
		});
	}

	@Environment(EnvType.CLIENT)
	public static void clientInit() {
		TrinketRendererRegistry.registerRenderer(HexItems.SCRYING_LENS, new LensTrinketRenderer());
	}
}
