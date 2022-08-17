package at.petrak.hexcasting.fabric.interop.trinkets;

import at.petrak.hexcasting.api.misc.DiscoveryHandlers;
import at.petrak.hexcasting.api.utils.ManaHelper;
import at.petrak.hexcasting.common.items.magic.DebugUnlockerHolder;
import at.petrak.hexcasting.common.items.magic.ItemCreativeUnlocker;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Tuple;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TrinketsApiInterop {
	public static void init() {
		DiscoveryHandlers.addLensPredicate(player -> {
			Optional<TrinketComponent> optional = TrinketsApi.getTrinketComponent(player);
			if (optional.isPresent()) {
				TrinketComponent component = optional.get();
				return component.isEquipped(HexItems.SCRYING_LENS);
			}
			return false;
		});

		DiscoveryHandlers.addManaHolderDiscoverer(harness -> {
			Optional<TrinketComponent> optional = TrinketsApi.getTrinketComponent(harness.getCtx().getCaster());
			if (optional.isPresent()) {
				TrinketComponent component = optional.get();
				return component.getEquipped(ManaHelper::isManaItem).stream()
					.map(Tuple::getB)
					.map(IXplatAbstractions.INSTANCE::findManaHolder)
					.filter(Objects::nonNull)
					.toList();
			}
			return List.of();
		});

		DiscoveryHandlers.addManaHolderDiscoverer(harness -> {
			Optional<TrinketComponent> optional = TrinketsApi.getTrinketComponent(harness.getCtx().getCaster());
			if (optional.isPresent()) {
				TrinketComponent component = optional.get();
				var equipped = component.getEquipped(ItemCreativeUnlocker::isDebug);
				if (!equipped.isEmpty()) {
					return List.of(new DebugUnlockerHolder(equipped.get(0).getB()));
				}
			}
			return List.of();
		});
	}

	@Environment(EnvType.CLIENT)
	public static void clientInit() {
		TrinketRendererRegistry.registerRenderer(HexItems.SCRYING_LENS, new LensTrinketRenderer());
	}
}
