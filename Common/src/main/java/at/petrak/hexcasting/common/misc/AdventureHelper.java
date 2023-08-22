package at.petrak.hexcasting.common.misc;

import net.minecraft.core.Vec3i;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.include.com.google.common.base.Strings;

/** Helper to limit usage for players in adventure mode
 * canUseLoose allows adventure-mode players to use a non-filtered version of the item
 * add `CanUseInDim` as a string tag to limit usable dimensions,
 * and add `CanUseInBox` as an int list tag to set a bounding box it can be used in (x1, y1, z1, x2, y2, z2)
 * Pass with a String prefix here to be able to use `CanUse<X>InDim` and `CanUse<X>InBox` for individual item actions
 */
public class AdventureHelper {

	public static boolean canUse(LivingEntity user, ItemStack stack, Level world, Vec3i pos) {
		return _canUse(user, null, stack, world, pos, false);
	}

	public static boolean canUse(LivingEntity user, ItemStack stack, Level world, Vec3 pos) {
		return _canUse(user, null, stack, world, pos, false);
	}

	public static boolean canUse(LivingEntity user, ItemStack stack, Level world, double x, double y, double z) {
		return _canUse(user, null, stack, world, x, y, z, false);
	}


	public static boolean canUseLoose(LivingEntity user, ItemStack stack, Level world, Vec3i pos) {
		return _canUse(user, null, stack, world, pos, true);
	}

	public static boolean canUseLoose(LivingEntity user, ItemStack stack, Level world, Vec3 pos) {
		return _canUse(user, null, stack, world, pos, true);
	}

	public static boolean canUseLoose(LivingEntity user, ItemStack stack, Level world, double x, double y, double z) {
		return _canUse(user, null, stack, world, x, y, z, true);
	}


	public static boolean canUse(LivingEntity user, String prefix, ItemStack stack, Level world, Vec3i pos) {
		return _canUse(user, prefix, stack, world, pos, false);
	}

	public static boolean canUse(LivingEntity user, String prefix, ItemStack stack, Level world, Vec3 pos) {
		return _canUse(user, prefix, stack, world, pos, false);
	}

	public static boolean canUse(LivingEntity user, String prefix, ItemStack stack, Level world, double x, double y, double z) {
		return _canUse(user, prefix, stack, world, x, y, z, false);
	}


	public static boolean canUseLoose(LivingEntity user, String prefix, ItemStack stack, Level world, Vec3i pos) {
		return _canUse(user, prefix, stack, world, pos, true);
	}

	public static boolean canUseLoose(LivingEntity user, String prefix, ItemStack stack, Level world, Vec3 pos) {
		return _canUse(user, prefix, stack, world, pos, true);
	}

	public static boolean canUseLoose(LivingEntity user, String prefix, ItemStack stack, Level world, double x, double y, double z) {
		return _canUse(user, prefix, stack, world, x, y, z, true);
	}


	private static boolean _canUse(LivingEntity user, String prefix, ItemStack stack, Level world, Vec3i pos, boolean loose) {
		return _canUse(user, prefix, stack, world, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, loose);
	}

	private static boolean _canUse(LivingEntity user, String prefix, ItemStack stack, Level world, Vec3 pos, boolean loose) {
		return _canUse(user, prefix, stack, world, pos.x, pos.y, pos.z, loose);
	}

	private static boolean _canUse(LivingEntity user, String prefix, ItemStack stack, Level world, double x, double y, double z, boolean loose) {
		prefix = Strings.nullToEmpty(prefix);
		if (user instanceof Player player) {
			boolean anyChecks = loose || player.mayBuild();
			boolean anyObjections = false;
			if (stack.hasTag() && stack.getTag().contains("CanUse"+prefix+"InDim", Tag.TAG_STRING)) {
				anyChecks = true;
				anyObjections |= !world.dimension().location().toString().equals(stack.getTag().getString("CanUse"+prefix+"InDim"));
			}
			if (stack.hasTag() && stack.getTag().contains("CanUse"+prefix+"InBox", Tag.TAG_INT_ARRAY)) {
				anyChecks = true;
				int[] arr = stack.getTag().getIntArray("CanUse"+prefix+"InBox");
				if (arr.length != 6) return false;
				int minX = Math.min(arr[0], arr[3]);
				int minY = Math.min(arr[1], arr[4]);
				int minZ = Math.min(arr[2], arr[5]);
				int maxX = Math.max(arr[0], arr[3]);
				int maxY = Math.max(arr[1], arr[4]);
				int maxZ = Math.max(arr[2], arr[5]);
				anyObjections |= !(x >= minX && x < maxX && y >= minY && y < maxY && z >= minZ && z < maxZ);
			}
			return anyChecks && !anyObjections;
		} else {
			return true; // I guess???
		}
	}

}