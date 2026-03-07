package at.petrak.hexcasting.api.casting.eval.vm.userdata.components

import at.petrak.hexcasting.api.HexAPI.modLoc
import at.petrak.hexcasting.api.casting.eval.vm.userdata.CastingImageComponent
import at.petrak.hexcasting.api.casting.eval.vm.userdata.ComponentType
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import java.util.UUID

data class ImpulseScalingComponent(val map: HashMap<UUID, Int>) : CastingImageComponent

object ImpulseScalingComponentType : ComponentType<ImpulseScalingComponent>(modLoc("impulse_scaling")) {
	override val transient: Boolean = true

	override fun serialize(value: ImpulseScalingComponent): CompoundTag {
		val tag = CompoundTag()
		value.map.forEach { (uuid, tax) -> tag.putInt(uuid.toString(), tax) }
		return tag
	}

	override fun deserialize(tag: CompoundTag, world: ServerLevel): ImpulseScalingComponent {
		val map = HashMap<UUID, Int>()
		tag.allKeys.forEach { uuid -> map[UUID.fromString(uuid)] = tag.getInt(uuid) }
		return ImpulseScalingComponent(map)
	}
}