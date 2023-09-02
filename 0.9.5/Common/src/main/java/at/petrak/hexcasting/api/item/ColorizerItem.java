package at.petrak.hexcasting.api.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

@ApiStatus.OverrideOnly
public interface ColorizerItem {
	int color(ItemStack stack, UUID owner, float time, Vec3 position);
}
