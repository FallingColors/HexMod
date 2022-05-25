package at.petrak.hexcasting.api.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public interface ColorizerItem {
	int color(ItemStack stack, UUID owner, float time, Vec3 position);
}
