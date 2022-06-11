@file:Suppress("NOTHING_TO_INLINE")
package at.petrak.hexcasting.api.utils

import net.minecraft.nbt.*

// https://github.com/TeamWizardry/LibrarianLib/blob/9cfb2cf3e35685568942ad41395265a2edc27d30/modules/core/src/main/kotlin/com/teamwizardry/librarianlib/core/util/kotlin/NbtBuilder.kt

@DslMarker
internal annotation class NBTDslMarker

@NBTDslMarker
object NBTBuilder {
    inline operator fun invoke(block: NbtCompoundBuilder.() -> Unit) = compound(block)
    inline operator fun invoke(tag: CompoundTag, block: NbtCompoundBuilder.() -> Unit) = use(tag, block)

    inline fun use(tag: CompoundTag, block: NbtCompoundBuilder.() -> Unit): CompoundTag =
        NbtCompoundBuilder(tag).apply(block).tag

    inline fun compound(block: NbtCompoundBuilder.() -> Unit): CompoundTag =
        NbtCompoundBuilder(CompoundTag()).apply(block).tag

    inline fun list(block: NbtListBuilder.() -> Unit): ListTag =
        NbtListBuilder(ListTag()).apply(block).tag

    inline fun list(vararg elements: Tag, block: NbtListBuilder.() -> Unit): ListTag =
        NbtListBuilder(ListTag()).also {
            it.addAll(elements.toList())
            it.block()
        }.tag

    inline fun list(vararg elements: Tag): ListTag = ListTag().also { it.addAll(elements) }
    inline fun list(elements: Collection<Tag>): ListTag = ListTag().also { it.addAll(elements) }
    inline fun <T> list(elements: Collection<T>, mapper: (T) -> Tag): ListTag = ListTag().also { it.addAll(elements.map(mapper)) }

    inline fun double(value: Number): DoubleTag = DoubleTag.valueOf(value.toDouble())
    inline fun float(value: Number): FloatTag = FloatTag.valueOf(value.toFloat())
    inline fun long(value: Number): LongTag = LongTag.valueOf(value.toLong())
    inline fun int(value: Number): IntTag = IntTag.valueOf(value.toInt())
    inline fun short(value: Number): ShortTag = ShortTag.valueOf(value.toShort())
    inline fun byte(value: Number): ByteTag = ByteTag.valueOf(value.toByte())

    inline fun string(value: String): StringTag = StringTag.valueOf(value)

    inline fun byteArray(value: Collection<Number>): ByteArrayTag = ByteArrayTag(value.map { it.toByte() })
    inline fun byteArray(vararg value: Int): ByteArrayTag = ByteArrayTag(ByteArray(value.size) { value[it].toByte() })
    inline fun byteArray(vararg value: Byte): ByteArrayTag = ByteArrayTag(value)
    inline fun byteArray(): ByteArrayTag = ByteArrayTag(byteArrayOf()) // avoiding overload ambiguity
    inline fun longArray(value: Collection<Number>): LongArrayTag = LongArrayTag(value.map { it.toLong() })
    inline fun longArray(vararg value: Int): LongArrayTag = LongArrayTag(LongArray(value.size) { value[it].toLong() })
    inline fun longArray(vararg value: Long): LongArrayTag = LongArrayTag(value)
    inline fun longArray(): LongArrayTag = LongArrayTag(longArrayOf()) // avoiding overload ambiguity
    inline fun intArray(value: Collection<Number>): IntArrayTag = IntArrayTag(value.map { it.toInt() })
    inline fun intArray(vararg value: Int): IntArrayTag = IntArrayTag(value)
}

@JvmInline
@NBTDslMarker
value class NbtCompoundBuilder(val tag: CompoundTag) {
    // configuring this tag

    inline operator fun String.remAssign(nbt: Tag) {
        tag.put(this, nbt)
    }

    inline operator fun String.remAssign(str: String) {
        tag.put(this, string(str))
    }

    inline operator fun String.remAssign(num: Int) {
        tag.put(this, int(num))
    }

    inline operator fun String.remAssign(num: Double) {
        tag.put(this, double(num))
    }

    inline operator fun String.remAssign(num: Float) {
        tag.put(this, float(num))
    }

    inline operator fun String.remAssign(bool: Boolean) {
        tag.put(this, byte(if (bool) 1 else 0))
    }

    // creating new tags

    inline fun compound(block: NbtCompoundBuilder.() -> Unit): CompoundTag =
        NbtCompoundBuilder(CompoundTag()).apply(block).tag

    inline fun list(block: NbtListBuilder.() -> Unit): ListTag =
        NbtListBuilder(ListTag()).apply(block).tag

    inline fun list(vararg elements: Tag, block: NbtListBuilder.() -> Unit): ListTag =
        NbtListBuilder(ListTag()).also {
            it.addAll(elements.toList())
            it.block()
        }.tag

    inline fun list(vararg elements: Tag): ListTag = ListTag().also { it.addAll(elements) }
    inline fun list(elements: Collection<Tag>): ListTag = ListTag().also { it.addAll(elements) }
    inline fun <T> list(elements: Collection<T>, mapper: (T) -> Tag): ListTag = ListTag().also { it.addAll(elements.map(mapper)) }

    inline fun double(value: Number): DoubleTag = DoubleTag.valueOf(value.toDouble())
    inline fun float(value: Number): FloatTag = FloatTag.valueOf(value.toFloat())
    inline fun long(value: Number): LongTag = LongTag.valueOf(value.toLong())
    inline fun int(value: Number): IntTag = IntTag.valueOf(value.toInt())
    inline fun short(value: Number): ShortTag = ShortTag.valueOf(value.toShort())
    inline fun byte(value: Number): ByteTag = ByteTag.valueOf(value.toByte())

    inline fun string(value: String): StringTag = StringTag.valueOf(value)

    inline fun byteArray(value: Collection<Number>): ByteArrayTag = ByteArrayTag(value.map { it.toByte() })
    inline fun byteArray(vararg value: Int): ByteArrayTag = ByteArrayTag(ByteArray(value.size) { value[it].toByte() })
    inline fun byteArray(vararg value: Byte): ByteArrayTag = ByteArrayTag(value)
    inline fun byteArray(): ByteArrayTag = ByteArrayTag(byteArrayOf()) // avoiding overload ambiguity
    inline fun longArray(value: Collection<Number>): LongArrayTag = LongArrayTag(value.map { it.toLong() })
    inline fun longArray(vararg value: Int): LongArrayTag = LongArrayTag(LongArray(value.size) { value[it].toLong() })
    inline fun longArray(vararg value: Long): LongArrayTag = LongArrayTag(value)
    inline fun longArray(): LongArrayTag = LongArrayTag(longArrayOf()) // avoiding overload ambiguity
    inline fun intArray(value: Collection<Number>): IntArrayTag = IntArrayTag(value.map { it.toInt() })
    inline fun intArray(vararg value: Int): IntArrayTag = IntArrayTag(value)
}

@JvmInline
@NBTDslMarker
value class NbtListBuilder(val tag: ListTag) {
    // configuring this tag

    /**
     * Add the given tag to this list
     */
    inline operator fun Tag.unaryPlus() {
        tag.add(this)
    }

    /**
     * Add the given  tags to this list
     */
    inline operator fun Collection<Tag>.unaryPlus() {
        tag.addAll(this)
    }

    /**
     * Add the given tag to this list. This is explicitly defined for [ListTag] because otherwise there is overload
     * ambiguity between the [Tag] and [Collection]<Tag> methods.
     */
    inline operator fun ListTag.unaryPlus() {
        tag.add(this)
    }

    inline fun addAll(nbt: Collection<Tag>) {
        this.tag.addAll(nbt)
    }

    inline fun add(nbt: Tag) {
        this.tag.add(nbt)
    }

    // creating new tags

    inline fun compound(block: NbtCompoundBuilder.() -> Unit): CompoundTag =
        NbtCompoundBuilder(CompoundTag()).apply(block).tag

    inline fun list(block: NbtListBuilder.() -> Unit): ListTag =
        NbtListBuilder(ListTag()).apply(block).tag

    inline fun list(vararg elements: Tag, block: NbtListBuilder.() -> Unit): ListTag =
        NbtListBuilder(ListTag()).also {
            it.addAll(elements.toList())
            it.block()
        }.tag

    inline fun list(vararg elements: Tag): ListTag = ListTag().also { it.addAll(elements) }
    inline fun list(elements: Collection<Tag>): ListTag = ListTag().also { it.addAll(elements) }
    inline fun <T> list(elements: Collection<T>, mapper: (T) -> Tag): ListTag = ListTag().also { it.addAll(elements.map(mapper)) }

    inline fun double(value: Number): DoubleTag = DoubleTag.valueOf(value.toDouble())
    inline fun float(value: Number): FloatTag = FloatTag.valueOf(value.toFloat())
    inline fun long(value: Number): LongTag = LongTag.valueOf(value.toLong())
    inline fun int(value: Number): IntTag = IntTag.valueOf(value.toInt())
    inline fun short(value: Number): ShortTag = ShortTag.valueOf(value.toShort())
    inline fun byte(value: Number): ByteTag = ByteTag.valueOf(value.toByte())

    inline fun string(value: String): StringTag = StringTag.valueOf(value)

    inline fun byteArray(value: Collection<Number>): ByteArrayTag = ByteArrayTag(value.map { it.toByte() })
    inline fun byteArray(vararg value: Int): ByteArrayTag = ByteArrayTag(ByteArray(value.size) { value[it].toByte() })
    inline fun byteArray(vararg value: Byte): ByteArrayTag = ByteArrayTag(value)
    inline fun byteArray(): ByteArrayTag = ByteArrayTag(byteArrayOf()) // avoiding overload ambiguity
    inline fun longArray(value: Collection<Number>): LongArrayTag = LongArrayTag(value.map { it.toLong() })
    inline fun longArray(vararg value: Int): LongArrayTag = LongArrayTag(LongArray(value.size) { value[it].toLong() })
    inline fun longArray(vararg value: Long): LongArrayTag = LongArrayTag(value)
    inline fun longArray(): LongArrayTag = LongArrayTag(longArrayOf()) // avoiding overload ambiguity
    inline fun intArray(value: Collection<Number>): IntArrayTag = IntArrayTag(value.map { it.toInt() })
    inline fun intArray(vararg value: Int): IntArrayTag = IntArrayTag(value)

    inline fun doubles(vararg value: Int): List<DoubleTag> = value.map { DoubleTag.valueOf(it.toDouble()) }
    inline fun doubles(vararg value: Double): List<DoubleTag> = value.map { DoubleTag.valueOf(it) }
    inline fun floats(vararg value: Int): List<FloatTag> = value.map { FloatTag.valueOf(it.toFloat()) }
    inline fun floats(vararg value: Float): List<FloatTag> = value.map { FloatTag.valueOf(it) }
    inline fun longs(vararg value: Int): List<LongTag> = value.map { LongTag.valueOf(it.toLong()) }
    inline fun longs(vararg value: Long): List<LongTag> = value.map { LongTag.valueOf(it) }
    inline fun ints(vararg value: Int): List<IntTag> = value.map { IntTag.valueOf(it) }
    inline fun shorts(vararg value: Int): List<ShortTag> = value.map { ShortTag.valueOf(it.toShort()) }
    inline fun shorts(vararg value: Short): List<ShortTag> = value.map { ShortTag.valueOf(it) }
    inline fun bytes(vararg value: Int): List<ByteTag> = value.map { ByteTag.valueOf(it.toByte()) }
    inline fun bytes(vararg value: Byte): List<ByteTag> = value.map { ByteTag.valueOf(it) }

    fun strings(vararg value: String): List<StringTag> = value.map { StringTag.valueOf(it) }
}
