package at.petrak.hexcasting.api.utils

import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.*


object Vector3fUtils {
    val XP
        get() = Vector3f(1f, 0f, 0f)
    val XM
        get() = Vector3f(-1f, 0f, 0f)
    val YP
        get() = Vector3f(0f, 1f, 0f)
    val YM
        get() = Vector3f(0f, -1f, 0f)
    val ZP
        get() = Vector3f(0f, 0f, 1f)
    val ZM
        get() = Vector3f(0f, 0f, -1f)
}

fun Vector3f.rotationDegrees(degrees: Float): Quaternionf {
    val rads = degrees * 0.017453292f

    val g = sin(rads / 2.0f)
    val i = this.x * g
    val j = this.y * g
    val k = this.z * g
    val r = cos(rads / 2.0f)

    return Quaternionf(i, j, k, r)
}

object QuaternionfUtils {
    @JvmStatic
    val ONE: Quaternionf
        get() = Quaternionf(0.0f, 0.0f, 0.0f, 1.0f)

    @JvmStatic
    fun fromXYZDegrees(vector3f: Vector3f): Quaternionf {

        return fromXYZ(Math.toRadians(vector3f.x().toDouble()).toFloat(), Math.toRadians(vector3f.y().toDouble()).toFloat(), Math.toRadians(vector3f.z().toDouble()).toFloat())
    }

    @JvmStatic
    fun fromXYZ(vector3f: Vector3f): Quaternionf {
        return fromXYZ(vector3f.x(), vector3f.y(), vector3f.z())
    }

    @JvmStatic
    fun fromXYZ(f: Float, g: Float, h: Float): Quaternionf {
        val quaternion = ONE
        quaternion.mul(Quaternionf(sin((f / 2.0f)), 0.0f, 0.0f, cos((f / 2.0f))))
        quaternion.mul(Quaternionf(0.0f, sin((g / 2.0f)), 0.0f, cos((g / 2.0f))))
        quaternion.mul(Quaternionf(0.0f, 0.0f, sin((h / 2.0f)), cos((h / 2.0f))))
        return quaternion
    }
}

fun Quaternionf.toXYZ(): Vector3f {
    val f: Float = this.w() * this.w()
    val g: Float = this.x() * this.x()
    val h: Float = this.y() * this.y()
    val i: Float = this.z() * this.z()
    val j = f + g + h + i
    val k: Float = 2.0f * this.w() * this.x() - 2.0f * this.y() * this.z()
    val l = asin(k / j)
    return if (abs(k) > 0.999f * j)
        Vector3f(2.0f * atan2(this.x(), this.w()), l, 0.0f)
    else Vector3f(
            atan2(2.0f * this.y() * this.z() + 2.0f * this.x() * this.w(), f - g - h + i),
            l,
            atan2(2.0f * this.x() * this.y() + 2.0f * this.w() * this.z(), f + g - h - i))
}