package at.petrak.hexcasting.api.casting.eval.vm.components

import at.petrak.hexcasting.api.HexAPI.modLoc
import at.petrak.hexcasting.api.utils.putList
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import java.util.UUID

data class ImpulseScalingComponent(val impulsedEntities: HashSet<UUID>) : CastingImageComponent

object ImpulseScalingComponentType : ComponentType<ImpulseScalingComponent>(modLoc("impulse_scaling")) {
	override val transient: Boolean = true

	override fun serialize(value: ImpulseScalingComponent): CompoundTag {
		val set = ListTag().apply {
			value.impulsedEntities.forEach { uuid -> add(StringTag.valueOf(uuid.toString())) }
		}
		val tag = CompoundTag()
		tag.putList("set", set)
		return tag
	}

	override fun deserialize(tag: CompoundTag, world: ServerLevel): ImpulseScalingComponent {
		val set = HashSet<UUID>()
		tag.getList("set", Tag.TAG_STRING.toInt()).forEach { uuid -> set.add(UUID.fromString(uuid.asString)) }
		return ImpulseScalingComponent(set)
	}
}
