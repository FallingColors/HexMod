package at.petrak.hexcasting.api.player;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * @param timeLeft sentinel of -1 for infinite
 * @param radius   sentinel of negative for infinite
 */
public record FlightAbility(int timeLeft, ResourceKey<Level> dimension, Vec3 origin, double radius) {
}
