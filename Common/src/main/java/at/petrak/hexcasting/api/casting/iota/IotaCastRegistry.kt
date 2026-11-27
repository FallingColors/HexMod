package at.petrak.hexcasting.api.casting.iota

import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import net.minecraft.resources.ResourceLocation
import java.util.*
import java.util.function.Function

object IotaCastRegistry {
    private val REGISTRY: HashMap<ResourceLocation,HashMap<ResourceLocation,Function<Iota,Optional<Iota>>>> = HashMap()
    @Suppress("UNCHECKED_CAST")
    fun <A: Iota, B: Iota> registerIotaConverter(input: IotaType<A>, output: IotaType<B>, converter: Function<A,Optional<B>>) {
        val inputId = HexIotaTypes.REGISTRY.getKey(input)
            ?: throw IllegalArgumentException("Input IotaType must be registered with HexIotaTypes registry")
        val outputId = HexIotaTypes.REGISTRY.getKey(output)
            ?: throw IllegalArgumentException("Output IotaType mst be registered with HexIotaTypes registry")

        val converters = REGISTRY.getOrPut(
            inputId
        ) { HashMap() }

        if (converters.containsKey(outputId)) {
            throw IllegalArgumentException("Input IotaType (%s) already has a converter registered for Output IotaType (%s), This is a conflict.".format(inputId,outputId))
        }

        converters[outputId] = converter as Function<Iota,Optional<Iota>>//there is an unchecked cast here. but we already know A and B extend Iota, so it is fair game
    }

    fun tryIotaConversion(inputT: IotaType<*>, outputT: IotaType<*>, input: Iota): Optional<Iota> {
        if (input.type != inputT) {
            throw IllegalArgumentException("Input IotaType and Input Iota have different types")
        }

        val inputId = HexIotaTypes.REGISTRY.getKey(inputT)
            ?: throw IllegalArgumentException("Input IotaType must be registered with HexIotaTypes registry")
        val outputId = HexIotaTypes.REGISTRY.getKey(outputT)
            ?: throw IllegalArgumentException("Output IotaType mst be registered with HexIotaTypes registry")

        val converters = REGISTRY[inputId]
            ?: throw IllegalArgumentException("Input IotaType (%s) has no converters to any other iota type".format(inputId))
        val converter = converters[outputId]
            ?: throw IllegalArgumentException("Input IotaType (%s) has no converter to IotaType (%s)".format(inputId,outputId))

        return converter.apply(input)
    }
}

@Suppress("UNCHECKED_CAST")
fun <T: Iota> Iota.into(other: IotaType<T>): Optional<T> = IotaCastRegistry.tryIotaConversion(this.type,other,this) as Optional<T>

@Suppress("UNCHECKED_CAST")
fun <I: Iota, T: Iota> IotaType<T>.from(other: I): Optional<T> = IotaCastRegistry.tryIotaConversion(other.type,this,other) as Optional<T>
