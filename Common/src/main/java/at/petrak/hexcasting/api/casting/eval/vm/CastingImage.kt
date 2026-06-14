package at.petrak.hexcasting.api.casting.eval.vm

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.api.utils.getOrCreateCompound
import at.petrak.hexcasting.api.utils.putCompound
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.Entity
import java.util.Optional

/**
 * The state of a casting VM, containing the stack and all
 */
data class CastingImage(
    val stack: TreeList<Iota>,
    val parenCount: Int,
    val parenthesized: TreeList<ParenthesizedIota>,
    val escapeNext: Boolean,
    val opsConsumed: Long,
    val userData: CompoundTag
) {
    constructor() : this(TreeList.empty(), 0, TreeList.empty(), false, 0, CompoundTag())

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
     * Returns a copy of this with the provided iota added to the parenthesized list.
     */
    fun withNewParenthesized(iota: Iota): CastingImage {
        val newParens = this.parenthesized.appended(ParenthesizedIota(iota, false))
        return this.copy(parenthesized = newParens)
    }

    /**
     * Returns this image's ravenmind in an Optional wrapper.
     */
    fun ravenmind() : Optional<CompoundTag> {
        val tag = userData.getCompound(HexAPI.RAVENMIND_USERDATA)

        var result: CompoundTag? = null
        if (!tag.isEmpty) { result = tag }
        return Optional.ofNullable(result)
    }

    companion object {
        @JvmStatic
        val CODEC = RecordCodecBuilder.create<CastingImage> { inst ->
            inst.group(
                TreeList.codecOf(IotaType.TYPED_CODEC).fieldOf("stack").forGetter { it.stack },
                Codec.INT.fieldOf("open_parens").forGetter { it.parenCount },
                TreeList.codecOf(ParenthesizedIota.CODEC).fieldOf("parenthesized").forGetter { it.parenthesized },
                Codec.BOOL.fieldOf("escape_next").forGetter { it.escapeNext },
                Codec.LONG.fieldOf("ops_consumed").forGetter { it.opsConsumed },
                CompoundTag.CODEC.fieldOf("userData").forGetter { it.userData }
            ).apply(inst) { a, b, c, d, e, f ->
                CastingImage(a, b, c, d, e, f)
            }
        }.orElseGet(::CastingImage)
        @JvmStatic
        val STREAM_CODEC = StreamCodec.composite(
            IotaType.TYPED_STREAM_CODEC.apply(TreeList.streamCodecOp()), CastingImage::stack,
            ByteBufCodecs.VAR_INT, CastingImage::parenCount,
            ParenthesizedIota.STREAM_CODEC.apply(TreeList.streamCodecOp()), CastingImage::parenthesized,
            ByteBufCodecs.BOOL, CastingImage::escapeNext,
            ByteBufCodecs.VAR_LONG, CastingImage::opsConsumed,
            ByteBufCodecs.COMPOUND_TAG, { it.userData },
            { a, b, c, d, e, f ->
                        CastingImage(a, b, c, d, e, f)
                    }
        )

        @JvmStatic
        fun checkAndMarkGivenMotion(userData: CompoundTag, entity: Entity): Boolean {
            val marked = userData.getOrCreateCompound(HexAPI.MARKED_MOVED_USERDATA)
            return if (marked.contains(entity.stringUUID)) {
                true
            } else {
                marked.putBoolean(entity.stringUUID, true)
                userData.putCompound(HexAPI.MARKED_MOVED_USERDATA, marked)
                false
            }
        }
    }
}
