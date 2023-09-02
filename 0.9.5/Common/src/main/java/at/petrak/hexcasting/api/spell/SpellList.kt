package at.petrak.hexcasting.api.spell

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel

/**
 * Restricted interface for functional lists.
 *
 * ...Surely this won't have any performance implications.
 */
sealed class SpellList : Iterable<SpellDatum<*>> {

    abstract val nonEmpty: Boolean
    abstract val car: SpellDatum<*>
    abstract val cdr: SpellList

    class LPair(override val car: SpellDatum<*>, override val cdr: SpellList) : SpellList() {
        override val nonEmpty = true
    }

    class LList(val idx: Int, val list: List<SpellDatum<*>>) : SpellList() {
        override val nonEmpty: Boolean
            get() = idx < list.size
        override val car: SpellDatum<*>
            get() = list[idx]
        override val cdr: SpellList
            get() = LList(idx + 1, list)
    }

    fun modifyAt(startIdx: Int, modify: (SpellList) -> SpellList): SpellList {
        val stack = mutableListOf<SpellDatum<*>>()
        val ptr = iterator()
        var idx = startIdx
        if (idx < 0) {
            return this
        }
        while (idx > 0) {
            if (!ptr.hasNext()) {
                return this
            }
            idx--
            stack.add(ptr.next())
        }
        var value = modify(ptr.list)
        for (datum in stack.asReversed()) {
            value = LPair(datum, value)
        }
        return value
    }

    fun getAt(startIdx: Int): SpellDatum<*> {
        var ptr = this
        var idx = startIdx
        if (idx < 0) {
            throw ArrayIndexOutOfBoundsException()
        }
        while (idx > 0) {
            when (ptr) {
                is LPair -> ptr = ptr.cdr
                is LList -> return ptr.list[ptr.idx + idx]
            }
            idx--
        }
        return ptr.car
    }

    override fun toString() = toList().toString()

    override fun iterator() = SpellListIterator(this)

    class SpellListIterator(var list: SpellList) : Iterator<SpellDatum<*>> {
        override fun hasNext() = list.nonEmpty
        override operator fun next(): SpellDatum<*> {
            val car = list.car
            list = list.cdr
            return car
        }
    }

    companion object {
        @JvmStatic
        fun fromNBT(nbt: ListTag, world: ServerLevel): LList {
            val out = ArrayList<SpellDatum<*>>(nbt.size)
            for (subtag in nbt) {
                // this is safe because otherwise we wouldn't have been able to get the list before
                out.add(SpellDatum.fromNBT(subtag as CompoundTag, world))
            }
            return LList(0, out)
        }
    }
}
