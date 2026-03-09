package at.petrak.hexcasting.api.casting.eval.vm.components

import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import net.minecraft.resources.ResourceLocation

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
 * @param id The identifier for the type, e.g. `"hexcasting:ravenmind"`.
 * @param transient If `true`, components of this type are stripped by [CastingImage.removeTransientComponents].
 *                  Use this for per-cast state that must not bleed across spell-circle slate jumps or separate staff patterns.
 *                  Currently used only for impulse cost accumulator.
 */
abstract class ComponentType<T : CastingImageComponent>(val id: ResourceLocation) {
	open val transient: Boolean = false
	abstract fun serialize(value: T): CompoundTag
	abstract fun deserialize(tag: CompoundTag, world: ServerLevel): T

	@Suppress("UNCHECKED_CAST")
	fun uncheckedSerialize(value: Any): CompoundTag = serialize(value as T)
	fun safeDeserialize(tag: CompoundTag, world: ServerLevel): T? = runCatching { deserialize(tag, world) }.getOrNull()
	override fun equals(other: Any?) = other is ComponentType<*> && other.id == id
	override fun hashCode() = id.hashCode()
}