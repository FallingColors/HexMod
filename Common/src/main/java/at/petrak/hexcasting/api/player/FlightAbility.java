package at.petrak.hexcasting.api.player;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record FlightAbility(boolean allowed, int timeLeft, ResourceKey<Level> dimension, Vec3 origin, double radius) {
	public static FlightAbility deny() {
		return new FlightAbility(false, 0, Level.OVERWORLD, Vec3.ZERO, 0);
	}
}
