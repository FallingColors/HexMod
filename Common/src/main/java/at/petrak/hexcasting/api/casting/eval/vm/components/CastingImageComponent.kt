package at.petrak.hexcasting.api.casting.eval.vm.components

import net.minecraft.nbt.CompoundTag
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.common.lib.HexRegistries
import at.petrak.hexcasting.xplat.IXplatAbstractions
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

/**
 * A single instance of component data attached to a [CastingImage].
 *
 * Components are the replacement for storing arbitrary data in [CastingImage]s.
 * Instead of reaching into an untyped [CompoundTag] and constantly deserializing / reserializing, you declare a [ComponentType],
 * register it, and patterns simply call [CastingImage.getComponent] or [CastingImage.withComponent].
 */
interface CastingImageComponent

/**
 * Describes a type of component: how to serialize, deserialize, and whether components of this type are transient.
 * There may only be one component of a given type per [CastingImage].
 *
 * @param T The [CastingImageComponent] type this describes.
 * @param id A name for the type, to distinguish it from other types with the same kind of [CastingImageComponent] (e.g. `"ravenmind"`).
 * @param transient If `true`, components of this type are stripped by [CastingImage.removeTransientComponents].
 *                  Use this for per-cast state that must not bleed across spell-circle slate jumps or separate staff patterns.
 *                  Currently used only for impulse cost accumulator.
 */
abstract class ComponentType<T : CastingImageComponent>(val id: String) {
	open val transient: Boolean = false

	abstract fun componentCodec(): MapCodec<T>
	abstract fun componentStreamCodec(): StreamCodec<RegistryFriendlyByteBuf, T>

	override fun equals(other: Any?) = other is ComponentType<*> && other.id == id
	override fun hashCode() = id.hashCode()

	companion object {
		val CODEC: Codec<ComponentType<*>> = IXplatAbstractions.INSTANCE.imageComponentRegistry.byNameCodec()
		val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ComponentType<*>> = ByteBufCodecs.registry(HexRegistries.IMAGE_COMPONENT)
	}
}