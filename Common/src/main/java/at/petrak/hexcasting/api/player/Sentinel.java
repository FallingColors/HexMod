package at.petrak.hexcasting.api.player;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record Sentinel(boolean hasSentinel, boolean extendsRange, Vec3 position,
					   ResourceKey<Level> dimension) {
	public static Sentinel none() {
		return new Sentinel(false, false, Vec3.ZERO, Level.OVERWORLD);
	}


}
