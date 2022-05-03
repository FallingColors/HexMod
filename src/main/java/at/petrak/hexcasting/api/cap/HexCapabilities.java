package at.petrak.hexcasting.api.cap;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

import java.util.Optional;

public final class HexCapabilities {

	public static final Capability<ManaHolder> MANA = CapabilityManager.get(new CapabilityToken<>() {
	});
	public static final Capability<DataHolder> DATUM = CapabilityManager.get(new CapabilityToken<>() {
	});
	public static final Capability<SpellHolder> SPELL = CapabilityManager.get(new CapabilityToken<>() {
	});
	public static final Capability<Colorizer> COLOR = CapabilityManager.get(new CapabilityToken<>() {
	});

	public static <T> Optional<T> getCapability(ItemStack stack, Capability<T> cap) {
		if (stack.isEmpty())
			return Optional.empty();
		return stack.getCapability(cap).resolve();
	}
}
