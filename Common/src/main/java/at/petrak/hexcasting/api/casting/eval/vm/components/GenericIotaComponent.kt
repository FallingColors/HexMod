package at.petrak.hexcasting.api.casting.eval.vm.components

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel

data class GenericIotaComponent(val iota: Iota) : CastingImageComponent

class GenericIotaComponentType(id: String) : ComponentType<GenericIotaComponent>(id) {
	override fun componentCodec(): MapCodec<GenericIotaComponent> = IotaType.TYPED_CODEC
		.xmap(::GenericIotaComponent, GenericIotaComponent::iota)
		.fieldOf("iota")

	override fun componentStreamCodec(): StreamCodec<RegistryFriendlyByteBuf, GenericIotaComponent> = IotaType.TYPED_STREAM_CODEC
		.map(::GenericIotaComponent, GenericIotaComponent::iota)
		.mapStream{ buffer -> buffer }
}