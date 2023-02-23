package at.petrak.hexcasting.api.player;

/**
 * Note that this just keeps track of state, actually giving the player the elytra ability is handled
 * differently per platform
 *
 * @param gracePeriod so the flight isn't immediately removed because the player started on the ground
 */
public record AltioraAbility(int gracePeriod) {
}
