package at.petrak.hexcasting

import net.minecraft.nbt.LongArrayTag
import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import kotlin.math.max
import kotlin.math.min

object HexUtils {
    @JvmStatic
    fun Vec3.serializeToNBT(): LongArrayTag =
        LongArrayTag(longArrayOf(this.x.toRawBits(), this.y.toRawBits(), this.z.toRawBits()))

    @JvmStatic
    fun DeserializeVec3FromNBT(tag: LongArray): Vec3 =
        Vec3(
            Double.fromBits(tag[0]),
            Double.fromBits(tag[1]),
            Double.fromBits(tag[2])
        )

    @JvmStatic
    fun Vec2.serializeToNBT(): LongArrayTag =
        LongArrayTag(longArrayOf(this.x.toDouble().toRawBits(), this.y.toDouble().toRawBits()))

    @JvmStatic
    fun DeserializeVec2FromNBT(tag: LongArray): Vec2 =
        Vec2(
            Double.fromBits(tag[0]).toFloat(),
            Double.fromBits(tag[1]).toFloat(),
        )

    @JvmStatic
    fun OtherHand(hand: InteractionHand) =
        if (hand == InteractionHand.MAIN_HAND) InteractionHand.OFF_HAND else InteractionHand.MAIN_HAND

    @JvmStatic
    fun FixNANs(x: Double): Double = if (x.isFinite()) x else 0.0

    @JvmStatic
    fun FindCenter(points: List<Vec2>): Vec2 {
        var minX = Float.POSITIVE_INFINITY
        var minY = Float.POSITIVE_INFINITY
        var maxX = Float.NEGATIVE_INFINITY
        var maxY = Float.NEGATIVE_INFINITY

        for (pos in points) {
            minX = min(minX, pos.x)
            minY = min(minY, pos.y)
            maxX = max(maxX, pos.x)
            maxY = max(maxY, pos.y)
        }
        return Vec2(
            (minX + maxX) / 2f,
            (minY + maxY) / 2f
        )
    }

    const val TAU = Math.PI * 2.0
}