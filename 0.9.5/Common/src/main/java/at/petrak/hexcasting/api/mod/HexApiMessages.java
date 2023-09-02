package at.petrak.hexcasting.api.mod;

// Don't understand what this does so i commented it all out :gigachad:
/*

import at.petrak.hexcasting.api.misc.FrozenColorizer;
import at.petrak.hexcasting.api.player.Sentinel;
import at.petrak.hexcasting.api.spell.ParticleSpray;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.BiFunction;
import java.util.function.Function;

@ApiStatus.Internal
public final class HexApiMessages {
	private static SimpleChannel channel;
	private static Function<Sentinel, Object> sentinelMessage;
	private static Function<FrozenColorizer, Object> colorizerMessage;
	private static BiFunction<ParticleSpray, FrozenColorizer, Object> particleSprayMessage;

	public static void setSyncChannel(SimpleChannel channel,
									  Function<Sentinel, Object> sentinelMessage,
									  Function<FrozenColorizer, Object> colorizerMessage,
									  BiFunction<ParticleSpray, FrozenColorizer, Object> particleSprayMessage) {
		if (HexApiMessages.channel != null)
			throw new IllegalStateException("Already set sync channel! If you're not Hex, you shouldn't be calling this.");
		HexApiMessages.channel = channel;
		HexApiMessages.sentinelMessage = sentinelMessage;
		HexApiMessages.colorizerMessage = colorizerMessage;
		HexApiMessages.particleSprayMessage = particleSprayMessage;
	}

	public static SimpleChannel getChannel() {
		return channel;
	}

	public static Object getColorizerMessage(FrozenColorizer colorizer) {
		return colorizerMessage.apply(colorizer);
	}

	public static Object getSentinelMessage(Sentinel colorizer) {
		return sentinelMessage.apply(colorizer);
	}

	public static Object getParticleSprayMessage(ParticleSpray spray, FrozenColorizer colorizer) {
		return particleSprayMessage.apply(spray, colorizer);
	}
}

 */
