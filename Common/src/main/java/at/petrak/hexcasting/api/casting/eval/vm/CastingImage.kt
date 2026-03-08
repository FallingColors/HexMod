package at.petrak.hexcasting.api.casting.eval.vm

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage.ParenthesizedIota.Companion.TAG_ESCAPED
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage.ParenthesizedIota.Companion.TAG_IOTAS
import at.petrak.hexcasting.api.casting.eval.vm.components.CastingImageComponent
import at.petrak.hexcasting.api.casting.eval.vm.components.CastingImageComponents
import at.petrak.hexcasting.api.casting.eval.vm.components.ComponentType
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.api.utils.asCompound
import at.petrak.hexcasting.api.utils.compositeCodecSeven
import at.petrak.hexcasting.api.utils.downcast
import at.petrak.hexcasting.api.utils.getList
import at.petrak.hexcasting.api.utils.getOrCreateCompound
import at.petrak.hexcasting.api.utils.putCompound
import at.petrak.hexcasting.api.utils.zipWithDefault
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import java.util.*

/**
 * The state of a casting VM, containing the stack and all
 */
data class CastingImage(
    val stack: TreeList<Iota>,
    val parenCount: Int,
    val parenthesized: TreeList<ParenthesizedIota>,
    val escapeNext: Boolean,
    val simulateNext: Boolean,
    val opsConsumed: Long,
    val components: Map<ComponentType<*>, CastingImageComponent>
) {
    constructor() : this(TreeList.empty(), 0, TreeList.empty(), false, false, 0, emptyMap())

    /**
     * `escaped` is used by [OpUndo][at.petrak.hexcasting.common.casting.actions.escaping.OpUndo] to determine whether the paren count
     * needs to be adjusted when undoing an open or close paren pattern (if the pattern was escaped, no need to change anything).
     */
    data class ParenthesizedIota(val iota: Iota, val escaped: Boolean) {
        companion object {
            val CODEC = RecordCodecBuilder.create<ParenthesizedIota> { inst ->
                inst.group(
                    IotaType.TYPED_CODEC.fieldOf("iota").forGetter { it.iota },
                    Codec.BOOL.fieldOf("escaped").forGetter { it.escaped }
                ).apply(inst, ::ParenthesizedIota)
            }
            val STREAM_CODEC = StreamCodec.composite(
                IotaType.TYPED_STREAM_CODEC, ParenthesizedIota::iota,
                ByteBufCodecs.BOOL, ParenthesizedIota::escaped,
                ::ParenthesizedIota
            )
        }
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
    fun withResetEscape() = this.copy(parenCount = 0, parenthesized = TreeList.empty(), escapeNext = false)

    /**
     * Returns a copy of this with the provided iota added to the parenthesized list. `escaped` is used by
     * [OpUndo][at.petrak.hexcasting.common.casting.actions.escaping.OpUndo] to determine whether undoing the
     * parenthesized iota should adjust the paren count (escaped parens do not affect the count).
     */
    fun withNewParenthesized(iota: Iota, escaped: Boolean = false): CastingImage {
        val newParens = this.parenthesized.appended(ParenthesizedIota(iota, escaped))
        return this.copy(parenthesized = newParens)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : CastingImageComponent> getComponent(type: ComponentType<T>): T? = this.components[type] as? T
    fun <T : CastingImageComponent> withComponent(type: ComponentType<T>, value: T): CastingImage = copy(components = this.components + (type to value))
    fun <T : CastingImageComponent> withoutComponent(type: ComponentType<T>): CastingImage = copy(components = this.components - type)
    fun removeTransientComponents(): CastingImage = copy(components = this.components.filterKeys { !it.transient })

//    fun serializeToNbt() = NBTBuilder {
//        TAG_STACK %= stack.serializeToNBT()
//
//        TAG_PAREN_COUNT %= parenCount
//        TAG_ESCAPE_NEXT %= escapeNext
//        TAG_PARENTHESIZED %= parenthesized.serializeToNBT()
//        TAG_OPS_CONSUMED %= opsConsumed
//
//        val componentsTag = CompoundTag()
//        for ((type, component) in components) {
//            val serialized = type.uncheckedSerialize(component)
//            componentsTag.put(type.id.toString(), serialized)
//        }
//        TAG_COMPONENTS %= componentsTag
//    }

    /**
     * Returns this image's ravenmind in an Optional wrapper.
     */
    fun ravenmind() : Optional<Iota> {
        val tag = userData.getCompound(HexAPI.RAVENMIND_USERDATA)

        var result: Iota? = null
        if (!tag.isEmpty) { result = IotaType.TYPED_CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow() }
        return Optional.ofNullable(result)
    }

    companion object {
//        const val TAG_STACK = "stack"
//        const val TAG_PAREN_COUNT = "open_parens"
//        const val TAG_PARENTHESIZED = "parenthesized"
//        const val TAG_ESCAPE_NEXT = "escape_next"
//        const val TAG_OPS_CONSUMED = "ops_consumed"
//        const val TAG_COMPONENTS = "components"
//
//        @JvmStatic
//        fun loadFromNbt(tag: CompoundTag, world: ServerLevel): CastingImage {
//            return try {
//                val stack = mutableListOf<Iota>()
//                val stackTag = tag.getList(TAG_STACK, Tag.TAG_COMPOUND)
//                for (subtag in stackTag) {
//                    val datum = IotaType.deserialize(subtag.asCompound, world)
//                    stack.add(datum)
//                }
//
//                val components = mutableMapOf<ComponentType<*>, CastingImageComponent>()
//                if (tag.contains(TAG_COMPONENTS, Tag.TAG_COMPOUND.toInt())) {
//                    val componentsTag = tag.getCompound(TAG_COMPONENTS)
//                    for (id in componentsTag.allKeys) {
//                        val type = CastingImageComponents.getById(ResourceLocation(id)) ?: continue
//                        val value = type.safeDeserialize(componentsTag.getCompound(id), world) ?: continue
//                        components[type] = value
//                    }
//                }
//
//                val parenthesized = mutableListOf<ParenthesizedIota>()
//                val parenTag = tag.getCompound(TAG_PARENTHESIZED)
//                val parenIotasTag = parenTag.getList(TAG_IOTAS, Tag.TAG_COMPOUND)
//                val parenEscapedTag = parenTag.getByteArray(TAG_ESCAPED)
//
//                for ((subtag, isEscapedByte) in parenIotasTag.zipWithDefault(parenEscapedTag) { _ -> 0 }) {
//                    parenthesized.add(ParenthesizedIota(IotaType.deserialize(subtag.downcast(CompoundTag.TYPE), world), isEscapedByte != 0.toByte()))
//                }
//
//                val parenCount = tag.getInt(TAG_PAREN_COUNT)
//                val escapeNext = tag.getBoolean(TAG_ESCAPE_NEXT)
//                val opsUsed = tag.getLong(TAG_OPS_CONSUMED)
//
//                CastingImage(stack, parenCount, parenthesized, escapeNext, opsUsed, components)
//            } catch (exn: Exception) {
//                HexAPI.LOGGER.warn("error while loading a CastingImage", exn)
//                CastingImage()
//            }
//        }

        @JvmStatic
        val CODEC = RecordCodecBuilder.create<CastingImage> { inst ->
            inst.group(
                TreeList.codecOf(IotaType.TYPED_CODEC).fieldOf("stack").forGetter { it.stack },
                Codec.INT.fieldOf("open_parens").forGetter { it.parenCount },
                TreeList.codecOf(ParenthesizedIota.CODEC).fieldOf("parenthesized").forGetter { it.parenthesized },
                Codec.BOOL.fieldOf("escape_next").forGetter { it.escapeNext },
                Codec.BOOL.fieldOf("simulate_next").forGetter { it.simulateNext },
                Codec.LONG.fieldOf("ops_consumed").forGetter { it.opsConsumed },
                CompoundTag.CODEC.fieldOf("userData").forGetter { it.userData }
            ).apply(inst) { a, b, c, d, e, f, g ->
                CastingImage(a, b, c, d, e, f, g)
            }
        }.orElseGet(::CastingImage)
        @JvmStatic
        val STREAM_CODEC = compositeCodecSeven(
            IotaType.TYPED_STREAM_CODEC.apply(TreeList.streamCodecOp()), CastingImage::stack,
            ByteBufCodecs.VAR_INT, CastingImage::parenCount,
            ParenthesizedIota.STREAM_CODEC.apply(TreeList.streamCodecOp()), CastingImage::parenthesized,
            ByteBufCodecs.BOOL, CastingImage::escapeNext,
            ByteBufCodecs.BOOL, CastingImage::simulateNext,
            ByteBufCodecs.VAR_LONG, CastingImage::opsConsumed,
            ByteBufCodecs.COMPOUND_TAG, { it.userData },
            { a, b, c, d, e, f, g ->
                        CastingImage(a, b, c, d, e, f, g)
                    }
        )
    }
}
