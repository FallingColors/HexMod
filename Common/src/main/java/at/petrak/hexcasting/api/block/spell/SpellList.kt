package at.petrak.hexcasting.api.spell

import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidSpellDatumType
import at.petrak.hexcasting.api.utils.HexUtils
import at.petrak.hexcasting.api.utils.HexUtils.serializeToNBT
import at.petrak.hexcasting.api.utils.getList
import net.minecraft.ChatFormatting
import net.minecraft.nbt.*
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import java.util.*

/**
 * Restricted interface for functional lists.
 *
 * ...Surely this won't have any performance implications.
 */
sealed class SpellList: Iterable<SpellDatum<*>> {

    abstract val nonEmpty: Boolean
    abstract val car: SpellDatum<*>
    abstract val cdr: SpellList

    class LPair(override val car: SpellDatum<*>, override val cdr: SpellList): SpellList() {
        override val nonEmpty = true
    }
    
    class LList(val idx: Int, val list: List<SpellDatum<*>>): SpellList() {
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

    override fun iterator() = Iterator(this)

    class Iterator(var list: SpellList): kotlin.collections.Iterator<SpellDatum<*>> {
        override fun hasNext() = list.nonEmpty
        override operator fun next(): SpellDatum<*> {
            val car = list.car
            list = list.cdr
            return car
        }
    }
}
