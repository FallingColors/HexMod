package at.petrak.hexcasting.api.casting.eval.vm.components

import at.petrak.hexcasting.api.HexAPI.modLoc
import net.minecraft.resources.ResourceLocation

object CastingImageComponents {
	val RAVENMIND: ComponentType<GenericIotaComponent> = register(GenericIotaComponentType(modLoc("ravenmind")))
	val IMPULSE_SCALING: ComponentType<ImpulseScalingComponent> = register(ImpulseScalingComponentType)

	private val registry = HashMap<ResourceLocation, ComponentType<*>>()

	fun <T : CastingImageComponent> register(type: ComponentType<T>): ComponentType<T> {
		val existing = registry.putIfAbsent(type.id, type)
		if (existing != null && existing != type)
			throw AssertionError("A component type of id '${type.id}' is already registered by a different object.")
		return type
	}

	fun getById(id: ResourceLocation): ComponentType<*>? = registry[id]
}