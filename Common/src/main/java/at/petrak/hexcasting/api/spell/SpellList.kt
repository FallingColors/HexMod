package at.petrak.hexcasting.api.spell

import at.petrak.hexcasting.api.spell.iota.Iota

/**
 * Restricted interface for functional lists.
 *
 * ...Surely this won't have any performance implications.
 */
sealed class SpellList : Iterable<Iota> {

    abstract val nonEmpty: Boolean
    abstract val car: Iota
    abstract val cdr: SpellList

    class LPair(override val car: Iota, override val cdr: SpellList) : SpellList() {
        override val nonEmpty = true
    }

    class LList(val idx: Int, val list: List<Iota>) : SpellList() {
        override val nonEmpty: Boolean
            get() = idx < list.size
        override val car: Iota
            get() = list[idx]
        override val cdr: SpellList
            get() = LList(idx + 1, list)

        constructor(list: List<Iota>) : this(0, list)
    }

    fun modifyAt(startIdx: Int, modify: (SpellList) -> SpellList): SpellList {
        val stack = mutableListOf<Iota>()
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

    fun getAt(startIdx: Int): Iota {
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

    /**
     * Note this is O(n), probably.
     */
    fun size(): Int {
        var size = 0
        var ptr = this
        while (ptr.nonEmpty) {
            ptr = ptr.cdr
            size++
        }
        return size
    }

    class SpellListIterator(var list: SpellList) : Iterator<Iota> {
        override fun hasNext() = list.nonEmpty
        override operator fun next(): Iota {
            val car = list.car
            list = list.cdr
            return car
        }
    }
}
