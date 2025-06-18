package at.petrak.hexcasting.api.casting.eval.vm

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage.ParenthesizedIota
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage.ParenthesizedIota.Companion.TAG_ESCAPED
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage.ParenthesizedIota.Companion.TAG_IOTAS
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.utils.*
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.entity.Entity
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

/**
 * The state of a casting VM, containing the stack and all
 */
data class CastingImage(
    val stack: List<Iota>,

    val parenCount: Int,
    val parenthesized: List<ParenthesizedIota>,
    val escapeNext: Boolean,
    val opsConsumed: Long,
    val userData: CompoundTag
) {
    constructor() : this(listOf(), 0, listOf(), false, 0, CompoundTag())

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

            const val TAG_IOTAS = "iotas"
            const val TAG_ESCAPED = "escaped"
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
    fun withResetEscape() = this.copy(parenCount = 0, parenthesized = listOf(), escapeNext = false)

    companion object {
        @JvmStatic
        val CODEC = RecordCodecBuilder.create<CastingImage> { inst ->
            inst.group(
                IotaType.TYPED_CODEC.listOf().fieldOf("stack").forGetter { it.stack },
                Codec.INT.fieldOf("open_parens").forGetter { it.parenCount },
                ParenthesizedIota.CODEC.listOf().fieldOf("parenthesized").forGetter { it.parenthesized },
                Codec.BOOL.fieldOf("escape_next").forGetter { it.escapeNext },
                Codec.LONG.fieldOf("ops_consumed").forGetter { it.opsConsumed },
                CompoundTag.CODEC.fieldOf("userData").forGetter { it.userData }
            ).apply(inst) { a, b, c, d, e, f ->
                CastingImage(a, b, c, d, e, f)
            }
        }.orElseGet(::CastingImage)
        @JvmStatic
        val STREAM_CODEC = StreamCodec.composite(
            IotaType.TYPED_STREAM_CODEC.apply(ByteBufCodecs.list()), CastingImage::stack,
            ByteBufCodecs.VAR_INT, CastingImage::parenCount,
            ParenthesizedIota.STREAM_CODEC.apply(ByteBufCodecs.list()), CastingImage::parenthesized,
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
                false
            }
        }
    }
}
