package at.petrak.hex

import net.minecraft.nbt.LongArrayTag
import net.minecraft.world.phys.Vec3

object HexUtils {
    @JvmStatic
    fun Vec3.serializeToNBT(): LongArrayTag =
        LongArrayTag(longArrayOf(this.x.toRawBits(), this.y.toRawBits(), this.z.toRawBits()))

    @JvmStatic
    fun deserializeVec3FromNBT(tag: LongArray): Vec3 =
        Vec3(
            Double.fromBits(tag[0]),
            Double.fromBits(tag[1]),
            Double.fromBits(tag[2])
        )

    const val TAU = Math.PI * 2.0
}