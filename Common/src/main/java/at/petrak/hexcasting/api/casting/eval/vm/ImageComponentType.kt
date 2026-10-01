package at.petrak.hexcasting.api.casting.eval.vm

import net.minecraft.nbt.CompoundTag
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.common.lib.HexRegistries
import at.petrak.hexcasting.common.lib.hex.HexImageComponents
import at.petrak.hexcasting.xplat.IXplatAbstractions
import com.mojang.serialization.Codec
import net.minecraft.Util
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

/**
 * Casting image components are the replacement for storing arbitrary data in [CastingImage]s.
 * Instead of reaching into an untyped [CompoundTag] and constantly deserializing / reserializing, you declare a [ImageComponentType],
 * register it, and patterns simply call [CastingImage.getComponent] or [CastingImage.withComponent].
 *
 * There may only be one component of a given type per [CastingImage].
 *
 * @param T The data actually stored by this component type. For instance, the ravenmind component stores an [Iota].
 * @param dataCodec The [Codec] used to encode the data stored in a component of this type.
 * @param dataStreamCodec The [StreamCodec] used to encode the data stored in a component of this type.
 * @param transient If `true`, components of this type are stripped by [CastingImage.removeTransientComponents].
 *                  Use this for per-cast state that must not bleed across spell-circle slate jumps or separate staff patterns.
 *                  Currently unused in the base mod, as impulse cost no longer scales as of 1.21, but may be useful for addons.
 */
class ImageComponentType<T : Any>(
	val dataCodec: Codec<T>,
	val dataStreamCodec: StreamCodec<RegistryFriendlyByteBuf, T>,
	val transient: Boolean = false
) {
	override fun toString(): String = Util.getRegisteredName(HexImageComponents.REGISTRY, this)

	companion object {
		val CODEC: Codec<ImageComponentType<*>> = IXplatAbstractions.INSTANCE.imageComponentRegistry.byNameCodec()
		val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ImageComponentType<*>> = ByteBufCodecs.registry(HexRegistries.IMAGE_COMPONENT)
	}
}