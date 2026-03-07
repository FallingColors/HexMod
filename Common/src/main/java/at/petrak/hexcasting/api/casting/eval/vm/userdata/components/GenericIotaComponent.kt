package at.petrak.hexcasting.api.casting.eval.vm.userdata.components

import at.petrak.hexcasting.api.casting.eval.vm.userdata.CastingImageComponent
import at.petrak.hexcasting.api.casting.eval.vm.userdata.ComponentType
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel

data class GenericIotaComponent(val iota: Iota) : CastingImageComponent

class GenericIotaComponentType(id: ResourceLocation) : ComponentType<GenericIotaComponent>(id) {
	override fun serialize(value: GenericIotaComponent): CompoundTag = IotaType.serialize(value.iota)
	override fun deserialize(tag: CompoundTag, world: ServerLevel): GenericIotaComponent = GenericIotaComponent(IotaType.deserialize(tag, world))
}