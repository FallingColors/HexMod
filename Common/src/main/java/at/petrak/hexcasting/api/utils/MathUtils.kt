package at.petrak.hexcasting.api.utils

import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.*

object MathUtils {
    @JvmStatic
    fun clamp(long: Long, min: Long, max: Long): Long
        =    if (long <= min) min
        else if (long >= max) max
        else                  long
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