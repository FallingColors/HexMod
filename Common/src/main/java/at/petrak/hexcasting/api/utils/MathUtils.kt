package at.petrak.hexcasting.api.utils

import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin

val Vector3fXP
    get() = Vector3f(1f, 0f, 0f)
val Vector3fXM
    get() = Vector3f(-1f, 0f, 0f)
val Vector3fYP
    get() = Vector3f(0f, 1f, 0f)
val Vector3fYM
    get() = Vector3f(0f, -1f, 0f)
val Vector3fZP
    get() = Vector3f(0f, 0f, 1f)
val Vector3fZM
    get() = Vector3f(0f, 0f, -1f)

fun Vector3f.rotationDegrees(degrees: Float): Quaternionf {
    val rads = degrees * 0.017453292f

    val g = sin(rads / 2.0f)
    val i = this.x * g
    val j = this.y * g
    val k = this.z * g
    val r = cos(rads / 2.0f)

    return Quaternionf(i, j, k, r)
}