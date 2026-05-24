package at.petrak.hexcasting.api.player;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Note that this just keeps track of state, actually giving the player the elytra ability is handled
 * differently per platform
 *
 * @param gracePeriod so the flight isn't immediately removed because the player started on the ground
 */
public record AltioraAbility(int gracePeriod) {
    public static final StreamCodec<RegistryFriendlyByteBuf, AltioraAbility> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(
            AltioraAbility::new,
            AltioraAbility::gracePeriod
    ).mapStream(b -> b);
}
