@file:JvmName("NBTHelper")

package at.petrak.hexcasting.api.utils

import at.petrak.hexcasting.common.components.VariantItemComponent
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.*
import net.minecraft.server.ServerAdvancementManager
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import java.util.*
import kotlin.jvm.optionals.getOrNull

private inline fun <T : Any, K, E> T?.getIf(key: K, predicate: T?.(K) -> Boolean, get: T.(K) -> E): E? =
    getIf(key, predicate, get, null)

private inline fun <T : Any, K, E> T?.getIf(key: K, predicate: T?.(K) -> Boolean, get: T.(K) -> E, default: E): E {
    if (this != null && predicate(key))
        return get(key)
    return default
}

fun AdvancementHolder.isChildOf(root: AdvancementHolder, serverAdvancementManager: ServerAdvancementManager): Boolean {
    var current = this
    while (true) {
        if (current.equals(root)) return true
        var parentOpt = serverAdvancementManager.get(current.value.parent.getOrNull() ?: return false) ?: return false

        current = parentOpt
    }
}

// ======================================================================================================== CompoundTag

// Checks for containment

fun CompoundTag?.hasNumber(key: String) = contains(key, Tag.TAG_ANY_NUMERIC)
fun CompoundTag?.hasByte(key: String) = contains(key, Tag.TAG_BYTE)
fun CompoundTag?.hasShort(key: String) = contains(key, Tag.TAG_SHORT)
fun CompoundTag?.hasInt(key: String) = contains(key, Tag.TAG_INT)
fun CompoundTag?.hasLong(key: String) = contains(key, Tag.TAG_LONG)
fun CompoundTag?.hasFloat(key: String) = contains(key, Tag.TAG_FLOAT)
fun CompoundTag?.hasDouble(key: String) = contains(key, Tag.TAG_DOUBLE)
fun CompoundTag?.hasLongArray(key: String) = contains(key, Tag.TAG_LONG_ARRAY)
fun CompoundTag?.hasIntArray(key: String) = contains(key, Tag.TAG_INT_ARRAY)
fun CompoundTag?.hasByteArray(key: String) = contains(key, Tag.TAG_BYTE_ARRAY)
fun CompoundTag?.hasCompound(key: String) = contains(key, Tag.TAG_COMPOUND)
fun CompoundTag?.hasString(key: String) = contains(key, Tag.TAG_STRING)
fun CompoundTag?.hasList(key: String) = contains(key, Tag.TAG_LIST)
fun CompoundTag?.hasList(key: String, objType: Int) = hasList(key, objType.toByte())
fun CompoundTag?.hasList(key: String, objType: Byte): Boolean {
    if (!hasList(key)) return false
    val lt = get(key) as ListTag
    return lt.elementType == objType || lt.elementType == 0.toByte()
}

fun CompoundTag?.hasUUID(key: String) = this != null && hasUUID(key)

fun CompoundTag?.contains(key: String, id: Byte) = contains(key, id.toInt())
fun CompoundTag?.contains(key: String, id: Int) = this != null && contains(key, id)
fun CompoundTag?.contains(key: String) = this != null && contains(key)

// Puts

fun CompoundTag?.putBoolean(key: String, value: Boolean) = this?.putBoolean(key, value)
fun CompoundTag?.putByte(key: String, value: Byte) = this?.putByte(key, value)
fun CompoundTag?.putShort(key: String, value: Short) = this?.putShort(key, value)
fun CompoundTag?.putInt(key: String, value: Int) = this?.putInt(key, value)
fun CompoundTag?.putLong(key: String, value: Long) = this?.putLong(key, value)
fun CompoundTag?.putFloat(key: String, value: Float) = this?.putFloat(key, value)
fun CompoundTag?.putDouble(key: String, value: Double) = this?.putDouble(key, value)
fun CompoundTag?.putLongArray(key: String, value: LongArray) = this?.putLongArray(key, value)
fun CompoundTag?.putIntArray(key: String, value: IntArray) = this?.putIntArray(key, value)
fun CompoundTag?.putByteArray(key: String, value: ByteArray) = this?.putByteArray(key, value)
fun CompoundTag?.putCompound(key: String, value: CompoundTag) = put(key, value)
fun CompoundTag?.putString(key: String, value: String) = this?.putString(key, value)
fun CompoundTag?.putList(key: String, value: ListTag) = put(key, value)
fun CompoundTag?.putUUID(key: String, value: UUID) = this?.putUUID(key, value)
fun CompoundTag?.put(key: String, value: Tag) = this?.put(key, value)

// Remove

fun CompoundTag?.remove(key: String) = this?.remove(key)

// Gets

@JvmOverloads
fun CompoundTag?.getBoolean(key: String, defaultExpected: Boolean = false) =
    getIf(key, CompoundTag?::hasNumber, CompoundTag::getBoolean, defaultExpected)

@JvmOverloads
fun CompoundTag?.getByte(key: String, defaultExpected: Byte = 0) =
    getIf(key, CompoundTag?::hasNumber, CompoundTag::getByte, defaultExpected)

@JvmOverloads
fun CompoundTag?.getShort(key: String, defaultExpected: Short = 0) =
    getIf(key, CompoundTag?::hasNumber, CompoundTag::getShort, defaultExpected)

@JvmOverloads
fun CompoundTag?.getInt(key: String, defaultExpected: Int = 0) =
    getIf(key, CompoundTag?::hasNumber, CompoundTag::getInt, defaultExpected)

@JvmOverloads
fun CompoundTag?.getLong(key: String, defaultExpected: Long = 0) =
    getIf(key, CompoundTag?::hasNumber, CompoundTag::getLong, defaultExpected)

@JvmOverloads
fun CompoundTag?.getFloat(key: String, defaultExpected: Float = 0f) =
    getIf(key, CompoundTag?::hasNumber, CompoundTag::getFloat, defaultExpected)

@JvmOverloads
fun CompoundTag?.getDouble(key: String, defaultExpected: Double = 0.0) =
    getIf(key, CompoundTag?::hasNumber, CompoundTag::getDouble, defaultExpected)

fun CompoundTag?.getLongArray(key: String) = getIf(key, CompoundTag?::hasLongArray, CompoundTag::getLongArray)
fun CompoundTag?.getIntArray(key: String) = getIf(key, CompoundTag?::hasIntArray, CompoundTag::getIntArray)
fun CompoundTag?.getByteArray(key: String) = getIf(key, CompoundTag?::hasByteArray, CompoundTag::getByteArray)
fun CompoundTag?.getCompound(key: String): CompoundTag? =
    getIf(key, CompoundTag?::hasCompound, CompoundTag::getCompound)

fun CompoundTag?.getString(key: String) = getIf(key, CompoundTag?::hasString, CompoundTag::getString)
fun CompoundTag?.getList(key: String, objType: Byte) = getList(key, objType.toInt())
fun CompoundTag?.getList(key: String, objType: Int) = getIf(key, { hasList(key, objType) }) { getList(it, objType) }
fun CompoundTag?.getUUID(key: String) = getIf(key, CompoundTag?::hasUUID, CompoundTag::getUUID)
fun CompoundTag?.get(key: String) = getIf(key, CompoundTag?::contains, CompoundTag::get)

@JvmSynthetic
@JvmName("getListByByte")
fun CompoundTag.getList(key: String, objType: Byte): ListTag = getList(key, objType.toInt())

// Get-or-create

fun CompoundTag.getOrCreateCompound(key: String): CompoundTag = getCompound(key) ?: CompoundTag().also { putCompound(key, it) }
fun CompoundTag.getOrCreateList(key: String, objType: Byte) = getOrCreateList(key, objType.toInt())
fun CompoundTag.getOrCreateList(key: String, objType: Int): ListTag = if (hasList(key, objType)) getList(key, objType) else ListTag().also { putList(key, it) }

// ================================================================================================================ Tag

val Tag.asBoolean get() = asByte == 0.toByte()
val Tag.asByte get() = (this as? NumericTag)?.asByte ?: 0.toByte()
val Tag.asShort get() = (this as? NumericTag)?.asShort ?: 0.toShort()
val Tag.asInt get() = (this as? NumericTag)?.asInt ?: 0
val Tag.asLong get() = (this as? NumericTag)?.asLong ?: 0L
val Tag.asFloat get() = (this as? NumericTag)?.asFloat ?: 0F
val Tag.asDouble get() = (this as? NumericTag)?.asDouble ?: 0.0

val Tag.asLongArray: LongArray
    get() = when (this) {
        is LongArrayTag -> this.asLongArray
        is IntArrayTag -> {
            val array = this.asIntArray
            LongArray(array.size) { array[it].toLong() }
        }
        is ByteArrayTag -> {
            val array = this.asByteArray
            LongArray(array.size) { array[it].toLong() }
        }
        else -> LongArray(0)
    }

val Tag.asIntArray: IntArray
    get() = when (this) {
        is IntArrayTag -> this.asIntArray
        is LongArrayTag -> {
            val array = this.asLongArray
            IntArray(array.size) { array[it].toInt() }
        }
        is ByteArrayTag -> {
            val array = this.asByteArray
            IntArray(array.size) { array[it].toInt() }
        }
        else -> IntArray(0)
    }

val Tag.asByteArray: ByteArray
    get() = when (this) {
        is ByteArrayTag -> this.asByteArray
        is LongArrayTag -> {
            val array = this.asLongArray
            ByteArray(array.size) { array[it].toByte() }
        }
        is IntArrayTag -> {
            val array = this.asIntArray
            ByteArray(array.size) { array[it].toByte() }
        }
        else -> ByteArray(0)
    }

val Tag.asCompound get() = this as? CompoundTag ?: CompoundTag()

// asString is defined in Tag
val Tag.asList get() = this as? ListTag ?: ListTag()
val Tag.asUUID: UUID get() = if (this is IntArrayTag && this.size == 4) NbtUtils.loadUUID(this) else UUID(0, 0)
<<<<<<< HEAD

// ========================================================================================================== ItemStack

// Checks for containment

fun ItemStack.hasNumber(key: String) = get(DataComponents.CUSTOM_DATA)?.copyTag().hasNumber(key)
fun ItemStack.hasByte(key: String) = get(DataComponents.CUSTOM_DATA)?.copyTag().hasByte(key)
fun ItemStack.hasShort(key: String) = get(DataComponents.CUSTOM_DATA)?.copyTag().hasShort(key)
fun ItemStack.hasInt(key: String) = get(DataComponents.CUSTOM_DATA)?.copyTag().hasInt(key)
fun ItemStack.hasLong(key: String) = get(DataComponents.CUSTOM_DATA)?.copyTag().hasLong(key)
fun ItemStack.hasFloat(key: String) = get(DataComponents.CUSTOM_DATA)?.copyTag().hasFloat(key)
fun ItemStack.hasDouble(key: String) = get(DataComponents.CUSTOM_DATA)?.copyTag().hasDouble(key)
fun ItemStack.hasLongArray(key: String) = get(DataComponents.CUSTOM_DATA)?.copyTag().hasLongArray(key)
fun ItemStack.hasIntArray(key: String) = get(DataComponents.CUSTOM_DATA)?.copyTag().hasIntArray(key)
fun ItemStack.hasByteArray(key: String) = get(DataComponents.CUSTOM_DATA)?.copyTag().hasByteArray(key)
fun ItemStack.hasCompound(key: String) = get(DataComponents.CUSTOM_DATA)?.copyTag().hasCompound(key)
fun ItemStack.hasString(key: String) = get(DataComponents.CUSTOM_DATA)?.copyTag().hasString(key)
fun ItemStack.hasList(key: String) = get(DataComponents.CUSTOM_DATA)?.copyTag().hasList(key)
fun ItemStack.hasList(key: String, objType: Int) = get(DataComponents.CUSTOM_DATA)?.copyTag().hasList(key, objType)
fun ItemStack.hasList(key: String, objType: Byte) = get(DataComponents.CUSTOM_DATA)?.copyTag().hasList(key, objType)
fun ItemStack.hasUUID(key: String) = get(DataComponents.CUSTOM_DATA)?.copyTag().hasUUID(key)

@JvmName("contains")
fun ItemStack.containsTag(key: String) = get(DataComponents.CUSTOM_DATA)?.contains(key)

@JvmName("contains")
fun ItemStack.containsTag(key: String, id: Byte) = get(DataComponents.CUSTOM_DATA)?.copyTag().contains(key, id)

@JvmName("contains")
fun ItemStack.containsTag(key: String, id: Int) = get(DataComponents.CUSTOM_DATA)?.copyTag().contains(key, id)

fun ItemStack.findVariantHolder() = get<VariantItemComponent?>(VariantItemComponent.COMPONENT_TYPE)


// Puts
private fun ItemStack.updateCustomData(block: CompoundTag.() -> Unit) {
    val old = this.get(DataComponents.CUSTOM_DATA)
    val tag = (old?.copyTag() ?: CompoundTag()).apply(block)
    this.set(DataComponents.CUSTOM_DATA, CustomData.of(tag))
}

fun ItemStack.putBoolean(key: String, value: Boolean) = updateCustomData { putBoolean(key, value) }fun ItemStack.putByte(key: String, value: Byte) = updateCustomData { putByte(key, value) }
fun ItemStack.putShort(key: String, value: Short) = updateCustomData { putShort(key, value) }
fun ItemStack.putInt(key: String, value: Int) = updateCustomData { putInt(key, value) }
fun ItemStack.putLong(key: String, value: Long) = updateCustomData { putLong(key, value) }
fun ItemStack.putFloat(key: String, value: Float) = updateCustomData { putFloat(key, value) }
fun ItemStack.putDouble(key: String, value: Double) = updateCustomData { putDouble(key, value) }

fun ItemStack.putLongArray(key: String, value: LongArray) = updateCustomData { putLongArray(key, value) }
fun ItemStack.putIntArray(key: String, value: IntArray) = updateCustomData { putIntArray(key, value) }
fun ItemStack.putByteArray(key: String, value: ByteArray) = updateCustomData { putByteArray(key, value) }
fun ItemStack.putCompound(key: String, value: CompoundTag) = updateCustomData { put(key, value) }
fun ItemStack.putString(key: String, value: String) = updateCustomData { putString(key, value) }
fun ItemStack.putList(key: String, value: ListTag) = updateCustomData { put(key, value) }
fun ItemStack.putUUID(key: String, value: UUID) = updateCustomData { putUUID(key, value) }

// Remove

fun ItemStack.remove(key: String) = updateCustomData { remove(key) }

// Gets

private fun ItemStack.customTag(): CompoundTag =
    get(DataComponents.CUSTOM_DATA)?.copyTag() ?: CompoundTag()


@JvmOverloads
fun ItemStack.getBoolean(key: String, defaultExpected: Boolean = false) =
    customTag().getBoolean(key).let { if (customTag().contains(key)) it else defaultExpected }

@JvmOverloads
fun ItemStack.getByte(key: String, defaultExpected: Byte = 0) =
    customTag().getByte(key).let { if (customTag().contains(key)) it else defaultExpected }

@JvmOverloads
fun ItemStack.getShort(key: String, defaultExpected: Short = 0) =
    customTag().getShort(key).let { if (customTag().contains(key)) it else defaultExpected }

@JvmOverloads
fun ItemStack.getInt(key: String, defaultExpected: Int = 0) =
    customTag().getInt(key).let { if (customTag().contains(key)) it else defaultExpected }

@JvmOverloads
fun ItemStack.getLong(key: String, defaultExpected: Long = 0) =
    customTag().getLong(key).let { if (customTag().contains(key)) it else defaultExpected }

@JvmOverloads
fun ItemStack.getFloat(key: String, defaultExpected: Float = 0f) =
    customTag().getFloat(key).let { if (customTag().contains(key)) it else defaultExpected }

@JvmOverloads
fun ItemStack.getDouble(key: String, defaultExpected: Double = 0.0) =
    customTag().getDouble(key).let { if (customTag().contains(key)) it else defaultExpected }

fun ItemStack.getLongArray(key: String) = customTag().getLongArray(key)
fun ItemStack.getIntArray(key: String) = customTag().getIntArray(key)
fun ItemStack.getByteArray(key: String) = customTag().getByteArray(key)
fun ItemStack.getCompound(key: String) = customTag().getCompound(key)
fun ItemStack.getString(key: String) = customTag().getString(key)
fun ItemStack.getList(key: String, objType: Int) = customTag().getList(key, objType)
fun ItemStack.getUUID(key: String) = customTag().getUUID(key)

@JvmName("get")
fun ItemStack.getTag(key: String) = customTag().get(key)

// Get-or-create

fun ItemStack.getOrCreateCompound(key: String): CompoundTag = customTag().getOrCreateCompound(key)
fun ItemStack.getOrCreateList(key: String, objType: Byte) = customTag().getOrCreateList(key, objType)
fun ItemStack.getOrCreateList(key: String, objType: Int) = customTag().getOrCreateList(key, objType)
=======
>>>>>>> refs/remotes/slava/devel/port-1.21
