@file:JvmName("NBTHelper")
package at.petrak.hexcasting.api.utils

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.world.item.ItemStack
import java.util.*

private inline fun <T: Any, K, E> T?.getIf(key: K, predicate: T?.(K) -> Boolean, get: T.(K) -> E): E? =
    getIf(key, predicate, get, null)

private inline fun <T: Any, K, E> T?.getIf(key: K, predicate: T?.(K) -> Boolean, get: T.(K) -> E, default: E): E {
    if (this != null && predicate(key))
        return get(key)
    return default
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
fun CompoundTag?.hasList(key: String, objType: Byte) = hasList(key) && (get(key) as ListTag).elementType == objType
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
fun CompoundTag?.getBoolean(key: String, defaultExpected: Boolean = false) = getIf(key, CompoundTag?::hasNumber, CompoundTag::getBoolean, defaultExpected)
@JvmOverloads
fun CompoundTag?.getByte(key: String, defaultExpected: Byte = 0) = getIf(key, CompoundTag?::hasNumber, CompoundTag::getByte, defaultExpected)
@JvmOverloads
fun CompoundTag?.getShort(key: String, defaultExpected: Short = 0) = getIf(key, CompoundTag?::hasNumber, CompoundTag::getShort, defaultExpected)
@JvmOverloads
fun CompoundTag?.getInt(key: String, defaultExpected: Int = 0) = getIf(key, CompoundTag?::hasNumber, CompoundTag::getInt, defaultExpected)
@JvmOverloads
fun CompoundTag?.getLong(key: String, defaultExpected: Long = 0) = getIf(key, CompoundTag?::hasNumber, CompoundTag::getLong, defaultExpected)
@JvmOverloads
fun CompoundTag?.getFloat(key: String, defaultExpected: Float = 0f) = getIf(key, CompoundTag?::hasNumber, CompoundTag::getFloat, defaultExpected)
@JvmOverloads
fun CompoundTag?.getDouble(key: String, defaultExpected: Double = 0.0) = getIf(key, CompoundTag?::hasNumber, CompoundTag::getDouble, defaultExpected)

fun CompoundTag?.getLongArray(key: String) = getIf(key, CompoundTag?::hasLongArray, CompoundTag::getLongArray)
fun CompoundTag?.getIntArray(key: String) = getIf(key, CompoundTag?::hasIntArray, CompoundTag::getIntArray)
fun CompoundTag?.getByteArray(key: String) = getIf(key, CompoundTag?::hasByteArray, CompoundTag::getByteArray)
fun CompoundTag?.getCompound(key: String): CompoundTag? = getIf(key, CompoundTag?::hasCompound, CompoundTag::getCompound)
fun CompoundTag?.getString(key: String) = getIf(key, CompoundTag?::hasString, CompoundTag::getString)
fun CompoundTag?.getList(key: String, objType: Int) = getIf(key, { hasList(key, objType) }) { getList(it, objType) }
fun CompoundTag?.getUUID(key: String) = getIf(key, CompoundTag?::hasUUID, CompoundTag::getUUID)
fun CompoundTag?.get(key: String) = getIf(key, CompoundTag?::contains, CompoundTag::get)

// Get-or-create

fun CompoundTag.getOrCreateCompound(key: String) = getCompound(key) ?: CompoundTag().also { putCompound(key, this) }

// ========================================================================================================== ItemStack

// Checks for containment

fun ItemStack.hasNumber(key: String) = tag.hasNumber(key)
fun ItemStack.hasByte(key: String) = tag.hasByte(key)
fun ItemStack.hasShort(key: String) = tag.hasShort(key)
fun ItemStack.hasInt(key: String) = tag.hasInt(key)
fun ItemStack.hasLong(key: String) = tag.hasLong(key)
fun ItemStack.hasFloat(key: String) = tag.hasFloat(key)
fun ItemStack.hasDouble(key: String) = tag.hasDouble(key)
fun ItemStack.hasLongArray(key: String) = tag.hasLongArray(key)
fun ItemStack.hasIntArray(key: String) = tag.hasIntArray(key)
fun ItemStack.hasByteArray(key: String) = tag.hasByteArray(key)
fun ItemStack.hasCompound(key: String) = tag.hasCompound(key)
fun ItemStack.hasString(key: String) = tag.hasString(key)
fun ItemStack.hasList(key: String) = tag.hasList(key)
fun ItemStack.hasList(key: String, objType: Int) = tag.hasList(key, objType)
fun ItemStack.hasList(key: String, objType: Byte) = tag.hasList(key, objType)
fun ItemStack.hasUUID(key: String) = tag.hasUUID(key)

@JvmName("contains")
fun ItemStack.containsTag(key: String) = tag.contains(key)
@JvmName("contains")
fun ItemStack.containsTag(key: String, id: Byte) = tag.contains(key, id)
@JvmName("contains")
fun ItemStack.containsTag(key: String, id: Int) = tag.contains(key, id)

// Puts

fun ItemStack.putBoolean(key: String, value: Boolean) = orCreateTag.putBoolean(key, value)
fun ItemStack.putByte(key: String, value: Byte) = orCreateTag.putByte(key, value)
fun ItemStack.putShort(key: String, value: Short) = orCreateTag.putShort(key, value)
fun ItemStack.putInt(key: String, value: Int) = orCreateTag.putInt(key, value)
fun ItemStack.putLong(key: String, value: Long) = orCreateTag.putLong(key, value)
fun ItemStack.putFloat(key: String, value: Float) = orCreateTag.putFloat(key, value)
fun ItemStack.putDouble(key: String, value: Double) = orCreateTag.putDouble(key, value)

fun ItemStack.putLongArray(key: String, value: LongArray) = orCreateTag.putLongArray(key, value)
fun ItemStack.putIntArray(key: String, value: IntArray) = orCreateTag.putIntArray(key, value)
fun ItemStack.putByteArray(key: String, value: ByteArray) = orCreateTag.putByteArray(key, value)
fun ItemStack.putCompound(key: String, value: CompoundTag) = putTag(key, value)
fun ItemStack.putString(key: String, value: String) = orCreateTag.putString(key, value)
fun ItemStack.putList(key: String, value: ListTag) = putTag(key, value)
fun ItemStack.putUUID(key: String, value: UUID) = orCreateTag.putUUID(key, value)
@JvmName("put")
fun ItemStack.putTag(key: String, value: Tag) = orCreateTag.put(key, value)

// Remove

fun ItemStack.remove(key: String) = removeTagKey(key)

// Gets

@JvmOverloads
fun ItemStack.getBoolean(key: String, defaultExpected: Boolean = false) = tag.getBoolean(key, defaultExpected)
@JvmOverloads
fun ItemStack.getByte(key: String, defaultExpected: Byte = 0) = tag.getByte(key, defaultExpected)
@JvmOverloads
fun ItemStack.getShort(key: String, defaultExpected: Short = 0) = tag.getShort(key, defaultExpected)
@JvmOverloads
fun ItemStack.getInt(key: String, defaultExpected: Int = 0) = tag.getInt(key, defaultExpected)
@JvmOverloads
fun ItemStack.getLong(key: String, defaultExpected: Long = 0) = tag.getLong(key, defaultExpected)
@JvmOverloads
fun ItemStack.getFloat(key: String, defaultExpected: Float = 0f) = tag.getFloat(key, defaultExpected)
@JvmOverloads
fun ItemStack.getDouble(key: String, defaultExpected: Double = 0.0) = tag.getDouble(key, defaultExpected)

fun ItemStack.getLongArray(key: String) = tag.getLongArray(key)
fun ItemStack.getIntArray(key: String) = tag.getIntArray(key)
fun ItemStack.getByteArray(key: String) = tag.getByteArray(key)
fun ItemStack.getCompound(key: String) = tag.getCompound(key)
fun ItemStack.getString(key: String) = tag.getString(key)
fun ItemStack.getList(key: String, objType: Int) = tag.getList(key, objType)
fun ItemStack.getUUID(key: String) = tag.getUUID(key)
@JvmName("get")
fun ItemStack.getTag(key: String) = tag.get(key)

// Get-or-create

fun ItemStack.getOrCreateCompound(key: String): CompoundTag = getOrCreateTagElement(key)
