package at.petrak.hexcasting.api.casting.eval.vm

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage.ParenthesizedIota.Companion.TAG_ESCAPED
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage.ParenthesizedIota.Companion.TAG_IOTAS
import at.petrak.hexcasting.api.casting.eval.vm.components.CastingImageComponent
import at.petrak.hexcasting.api.casting.eval.vm.components.CastingImageComponents
import at.petrak.hexcasting.api.casting.eval.vm.components.ComponentType
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.utils.*
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity

/**
 * The state of a casting VM, containing the stack and all
 */
data class CastingImage private constructor(
    val stack: List<Iota>,

    val parenCount: Int,
    val parenthesized: List<ParenthesizedIota>,
    val escapeNext: Boolean,
    val opsConsumed: Long,

    val components: Map<ComponentType<*>, CastingImageComponent>
) {
    constructor() : this(listOf(), 0, listOf(), false, 0, emptyMap())

    data class ParenthesizedIota(val iota: Iota, val escaped: Boolean) {
        companion object {
            const val TAG_IOTAS = "iotas"
            const val TAG_ESCAPED = "escaped"
        }
    }

    /**
     * Returns an empty list if it's too complicated.
     */
    private fun Iterable<ParenthesizedIota>.serializeToNBT(): CompoundTag {
        val tag = CompoundTag()

        if (IotaType.isTooLargeToSerialize(this.map { it.iota })) {
            tag.put(TAG_IOTAS, ListTag())
            tag.put(TAG_ESCAPED, ListTag())
        } else {
            tag.put(TAG_IOTAS, ListIota(this.map { it.iota }).serialize())
            tag.put(TAG_ESCAPED, this.map { it.escaped }.serializeToNBT())
        }

        return tag
    }

    /**
     * Return a copy of this with the given number of ops additionally exhausted
     */
    fun withUsedOps(count: Long) = this.copy(opsConsumed = this.opsConsumed + count)

    /**
     * Return a copy of this with 1 op used
     */
    fun withUsedOp() = this.withUsedOps(1)

    /**
     * Returns a copy of this with the [opsConsumed] replaced with [count].
     */
    fun withOverriddenUsedOps(count: Long) = this.copy(opsConsumed = count)

    /**
     * Returns a copy of this with escape/paren-related fields cleared.
     */
    fun withResetEscape() = this.copy(parenCount = 0, parenthesized = listOf(), escapeNext = false)

    @Suppress("UNCHECKED_CAST")
    fun <T : CastingImageComponent> getComponent(type: ComponentType<T>): T? = this.components[type] as? T
    fun <T : CastingImageComponent> withComponent(type: ComponentType<T>, value: T): CastingImage = copy(components = this.components + (type to value))
    fun <T : CastingImageComponent> withoutComponent(type: ComponentType<T>): CastingImage = copy(components = this.components - type)
    fun removeTransientComponents(): CastingImage = copy(components = this.components.filterKeys { !it.transient })

    fun serializeToNbt() = NBTBuilder {
        TAG_STACK %= stack.serializeToNBT()

        TAG_PAREN_COUNT %= parenCount
        TAG_ESCAPE_NEXT %= escapeNext
        TAG_PARENTHESIZED %= parenthesized.serializeToNBT()
        TAG_OPS_CONSUMED %= opsConsumed

        val componentsTag = CompoundTag()
        for ((type, component) in components) {
            val serialized = type.uncheckedSerialize(component)
            componentsTag.put(type.id.toString(), serialized)
        }
        TAG_COMPONENTS %= componentsTag
    }

    companion object {
        const val TAG_STACK = "stack"
        const val TAG_PAREN_COUNT = "open_parens"
        const val TAG_PARENTHESIZED = "parenthesized"
        const val TAG_ESCAPE_NEXT = "escape_next"
        const val TAG_OPS_CONSUMED = "ops_consumed"
        const val TAG_COMPONENTS = "components"

        @JvmStatic
        fun loadFromNbt(tag: CompoundTag, world: ServerLevel): CastingImage {
            return try {
                val stack = mutableListOf<Iota>()
                val stackTag = tag.getList(TAG_STACK, Tag.TAG_COMPOUND)
                for (subtag in stackTag) {
                    val datum = IotaType.deserialize(subtag.asCompound, world)
                    stack.add(datum)
                }

                val components = mutableMapOf<ComponentType<*>, CastingImageComponent>()
                if (tag.contains(TAG_COMPONENTS, Tag.TAG_COMPOUND.toInt())) {
                    val componentsTag = tag.getCompound(TAG_COMPONENTS)
                    for (id in componentsTag.allKeys) {
                        val type = CastingImageComponents.getById(ResourceLocation(id)) ?: continue
                        val value = type.safeDeserialize(componentsTag.getCompound(id), world) ?: continue
                        components[type] = value
                    }
                }

                val parenthesized = mutableListOf<ParenthesizedIota>()
                val parenTag = tag.getCompound(TAG_PARENTHESIZED)
                val parenIotasTag = parenTag.getList(TAG_IOTAS, Tag.TAG_COMPOUND)
                val parenEscapedTag = parenTag.getByteArray(TAG_ESCAPED)

                for ((subtag, isEscapedByte) in parenIotasTag.zipWithDefault(parenEscapedTag) { _ -> 0 }) {
                    parenthesized.add(ParenthesizedIota(IotaType.deserialize(subtag.downcast(CompoundTag.TYPE), world), isEscapedByte != 0.toByte()))
                }

                val parenCount = tag.getInt(TAG_PAREN_COUNT)
                val escapeNext = tag.getBoolean(TAG_ESCAPE_NEXT)
                val opsUsed = tag.getLong(TAG_OPS_CONSUMED)

                CastingImage(stack, parenCount, parenthesized, escapeNext, opsUsed, components)
            } catch (exn: Exception) {
                HexAPI.LOGGER.warn("error while loading a CastingImage", exn)
                CastingImage()
            }
        }
    }
}
