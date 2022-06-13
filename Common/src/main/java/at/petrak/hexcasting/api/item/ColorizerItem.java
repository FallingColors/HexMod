package at.petrak.hexcasting.api.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Items which can be used as a colorizer can implement this interface.
 * <p>
 * On both the Forge and Fabric sides, the registry will be scanned for all items which implement this interface,
 * and the appropriate cap/CC will be attached.
 */
public interface ColorizerItem {
    int color(ItemStack stack, UUID owner, float time, Vec3 position);
}
