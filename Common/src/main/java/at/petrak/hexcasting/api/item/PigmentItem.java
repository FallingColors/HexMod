package at.petrak.hexcasting.api.item;

import at.petrak.hexcasting.api.pigment.ColorProvider;
import java.util.UUID;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

/**
 * Items which can be used as a colorizer can implement this interface.
 *
 * <p>On both the Forge and Fabric sides, the registry will be scanned for all items which implement
 * this interface, and the appropriate cap/CC will be attached.
 */
@ApiStatus.OverrideOnly
public interface PigmentItem {
	ColorProvider provideColor(ItemStack stack, UUID owner);
}
