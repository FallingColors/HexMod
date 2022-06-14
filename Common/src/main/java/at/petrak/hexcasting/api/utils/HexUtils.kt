@file:JvmName("HexUtils")

package at.petrak.hexcasting.api.utils

import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.ListIota
import at.petrak.hexcasting.api.spell.math.HexCoord
import at.petrak.hexcasting.common.lib.HexIotaTypes
import net.minecraft.ChatFormatting
import net.minecraft.nbt.*
import net.minecraft.network.chat.*
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.reflect.KProperty

const val TAU = Math.PI * 2.0
const val SQRT_3 = 1.7320508f

fun Vec3.serializeToNBT(): LongArrayTag =
    LongArrayTag(longArrayOf(this.x.toRawBits(), this.y.toRawBits(), this.z.toRawBits()))

fun vecFromNBT(tag: LongArray): Vec3 = if (tag.size != 3) Vec3.ZERO else
    Vec3(
        Double.fromBits(tag[0]),
        Double.fromBits(tag[1]),
        Double.fromBits(tag[2])
    )

fun Vec2.serializeToNBT(): LongArrayTag =
    LongArrayTag(longArrayOf(this.x.toDouble().toRawBits(), this.y.toDouble().toRawBits()))

fun vec2FromNBT(tag: LongArray): Vec2 = if (tag.size != 2) Vec2.ZERO else
    Vec2(
        Double.fromBits(tag[0]).toFloat(),
        Double.fromBits(tag[1]).toFloat(),
    )

fun otherHand(hand: InteractionHand) =
    if (hand == InteractionHand.MAIN_HAND) InteractionHand.OFF_HAND else InteractionHand.MAIN_HAND

fun fixNAN(x: Double): Double = if (x.isFinite()) x else 0.0

fun findCenter(points: List<Vec2>): Vec2 {
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

fun coordToPx(coord: HexCoord, size: Float, offset: Vec2): Vec2 =
    Vec2(
        SQRT_3 * coord.q.toFloat() + SQRT_3 / 2.0f * coord.r.toFloat(),
        1.5f * coord.r.toFloat()
    ).scale(size).add(offset)

fun pxToCoord(px: Vec2, size: Float, offset: Vec2): HexCoord {
    val offsetted = px.add(offset.negated())
    var qf = (SQRT_3 / 3.0f * offsetted.x - 0.33333f * offsetted.y) / size
    var rf = (0.66666f * offsetted.y) / size

    val q = qf.roundToInt()
    val r = rf.roundToInt()
    qf -= q
    rf -= r
    return if (q.absoluteValue >= r.absoluteValue)
        HexCoord(q + (qf + 0.5f * rf).roundToInt(), r)
    else
        HexCoord(q, r + (rf + 0.5 * qf).roundToInt())
}

fun String.withStyle(op: (Style) -> Style): MutableComponent = asTextComponent.withStyle(op)
fun String.withStyle(style: Style): MutableComponent = asTextComponent.withStyle(style)
fun String.withStyle(formatting: ChatFormatting): MutableComponent = asTextComponent.withStyle(formatting)
fun String.withStyle(vararg formatting: ChatFormatting): MutableComponent = asTextComponent.withStyle(*formatting)

infix fun String.styledWith(op: (Style) -> Style) = withStyle(op)
infix fun String.styledWith(style: Style) = withStyle(style)
infix fun String.styledWith(formatting: ChatFormatting) = withStyle(formatting)

infix fun MutableComponent.styledWith(op: (Style) -> Style): MutableComponent = withStyle(op)
infix fun MutableComponent.styledWith(style: Style): MutableComponent = withStyle(style)
infix fun MutableComponent.styledWith(formatting: ChatFormatting): MutableComponent = withStyle(formatting)

val String.black get() = this styledWith ChatFormatting.BLACK
val MutableComponent.black get() = this styledWith ChatFormatting.BLACK

val String.darkBlue get() = this styledWith ChatFormatting.DARK_BLUE
val MutableComponent.darkBlue get() = this styledWith ChatFormatting.DARK_BLUE

val String.darkGreen get() = this styledWith ChatFormatting.DARK_GREEN
val MutableComponent.darkGreen get() = this styledWith ChatFormatting.DARK_GREEN

val String.darkAqua get() = this styledWith ChatFormatting.DARK_AQUA
val MutableComponent.darkAqua get() = this styledWith ChatFormatting.DARK_AQUA

val String.darkRed get() = this styledWith ChatFormatting.DARK_RED
val MutableComponent.darkRed get() = this styledWith ChatFormatting.DARK_RED

val String.darkPurple get() = this styledWith ChatFormatting.DARK_PURPLE
val MutableComponent.darkPurple get() = this styledWith ChatFormatting.DARK_PURPLE

val String.gold get() = this styledWith ChatFormatting.GOLD
val MutableComponent.gold get() = this styledWith ChatFormatting.GOLD

val String.gray get() = this styledWith ChatFormatting.GRAY
val MutableComponent.gray get() = this styledWith ChatFormatting.GRAY

val String.darkGray get() = this styledWith ChatFormatting.DARK_GRAY
val MutableComponent.darkGray get() = this styledWith ChatFormatting.DARK_GRAY

val String.blue get() = this styledWith ChatFormatting.BLUE
val MutableComponent.blue get() = this styledWith ChatFormatting.BLUE

val String.green get() = this styledWith ChatFormatting.GREEN
val MutableComponent.green get() = this styledWith ChatFormatting.GREEN

val String.aqua get() = this styledWith ChatFormatting.AQUA
val MutableComponent.aqua get() = this styledWith ChatFormatting.AQUA

val String.red get() = this styledWith ChatFormatting.RED
val MutableComponent.red get() = this styledWith ChatFormatting.RED

val String.lightPurple get() = this styledWith ChatFormatting.LIGHT_PURPLE
val MutableComponent.lightPurple get() = this styledWith ChatFormatting.LIGHT_PURPLE

val String.yellow get() = this styledWith ChatFormatting.YELLOW
val MutableComponent.yellow get() = this styledWith ChatFormatting.YELLOW

val String.white get() = this styledWith ChatFormatting.WHITE
val MutableComponent.white get() = this styledWith ChatFormatting.WHITE

val String.obfuscated get() = this styledWith ChatFormatting.OBFUSCATED
val MutableComponent.obfuscated get() = this styledWith ChatFormatting.OBFUSCATED

val String.bold get() = this styledWith ChatFormatting.BOLD
val MutableComponent.bold get() = this styledWith ChatFormatting.BOLD

val String.strikethrough get() = this styledWith ChatFormatting.STRIKETHROUGH
val MutableComponent.strikethrough get() = this styledWith ChatFormatting.STRIKETHROUGH

val String.underline get() = this styledWith ChatFormatting.UNDERLINE
val MutableComponent.underline get() = this styledWith ChatFormatting.UNDERLINE

val String.italic get() = this styledWith ChatFormatting.ITALIC
val MutableComponent.italic get() = this styledWith ChatFormatting.ITALIC

operator fun MutableComponent.plusAssign(component: Component) {
    append(component)
}

val String.asTextComponent get() = TextComponent(this)
val String.asTranslatedComponent get() = TranslatableComponent(this)

fun String.asTranslatedComponent(vararg args: Any) = TranslatableComponent(this, *args)

/**
 * Represents a value that the garbage collector is still allowed to collect.
 * To create an instance of a [WeakValue], use [weakReference] or [weakMapped].
 */
interface WeakValue<T> {
    var value: T?
}

/**
 * A weakly referenced value that relies directly on a [WeakReference].
 *
 * This means that if there are no other places where the contained object is referenced,
 * the reference will expire, and value contained within this reference will become null.
 */
private class WeakReferencedValue<T>(var reference: WeakReference<T>?) : WeakValue<T> {
    override var value: T?
        get() = reference?.get()
        set(value) {
            reference = value?.let(::WeakReference)
        }
}

/**
 * A weakly referenced value that relies on a [WeakHashMap].
 *
 * Unlike [WeakReferencedValue], it relies on the continued existence of something else (obtained by [keyGen]).
 * For example, this can be used to hold an entity, and have the reference expire when the world it's in is unloaded.
 */
private class WeakMappedValue<K, T>(val keyGen: (T) -> K) : WeakValue<T> {
    val reference = WeakHashMap<K, T>()
    override var value: T?
        get() = reference.values.firstOrNull()
        set(value) {
            reference.clear()
            if (value != null) reference[keyGen(value)] = value
        }
}

/**
 * Creates a [WeakReferencedValue], the contents of which will expire when nothing else is referencing them.
 */
fun <T> weakReference(value: T? = null): WeakValue<T> = WeakReferencedValue(value?.let { WeakReference(it) })

/**
 * Creates a [WeakMappedValue], the contents of which will expire when nothing else is referencing the value returned by [keyGen].
 */
fun <T, K> weakMapped(keyGen: (T) -> K): WeakValue<T> = WeakMappedValue(keyGen)

// kt boilerplate for making WeakValues work as delegates (using the keyword `by`)
@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> WeakValue<T>.getValue(thisRef: Any?, property: KProperty<*>): T? = value

@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> WeakValue<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
    this.value = value
}

/**
 * Returns an empty list if it's too complicated.
 */
fun Iterable<Iota>.serializeToNBT() =
    if (HexIotaTypes.isTooLargeToSerialize(this))
        ListTag()
    else
        ListIota(this.toList()).serialize()

// Copy the impl from forge
fun ItemStack.serializeToNBT(): CompoundTag {
    val out = CompoundTag()
    this.save(out)
    return out
}

@Throws(IllegalArgumentException::class)
fun <T : Tag> Tag.downcast(type: TagType<T>): T {
    if (this.type == type) {
        return this as T
    } else {
        throw IllegalArgumentException(
            "Expected this tag to be of type ${type.name}, but found ${this.type.name}."
        )
    }
}

const val ERROR_COLOR = 0xff_f800f8.toInt()
