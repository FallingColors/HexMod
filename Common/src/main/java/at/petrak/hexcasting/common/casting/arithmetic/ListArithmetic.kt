package at.petrak.hexcasting.common.casting.arithmetic

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic
import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic.*
import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator
import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator.downcast
import at.petrak.hexcasting.api.casting.arithmetic.operator.OperatorBinary
import at.petrak.hexcasting.api.casting.arithmetic.operator.OperatorUnary
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate.all
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate.pair
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.casting.arithmetic.operator.list.*
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes.LIST

object ListArithmetic : Arithmetic {
    private val OPS = listOf(
            INDEX,
            SLICE,
            APPEND,
            UNAPPEND,
            ADD,
            ABS,
            REV,
            INDEX_OF,
            REMOVE,
            REPLACE,
            CONS,
            UNCONS
    )

    override fun arithName() = "list_ops"

    override fun opTypes(): Iterable<HexPattern> = OPS

    override fun getOperator(pattern: HexPattern): Operator? {
        return when (pattern) {
            INDEX -> OperatorIndex
            SLICE -> OperatorSlice
            APPEND -> OperatorAppend
            UNAPPEND -> OperatorUnappend
            ADD -> OperatorConcat
            ABS -> OperatorUnary(all(IotaPredicate.ofType(LIST))) { iota: Iota -> DoubleIota(downcast(iota, LIST).list.size().toDouble()) }
            REV -> OperatorUnary(all(IotaPredicate.ofType(LIST))) { iota: Iota -> ListIota(downcast(iota, LIST).list.toList().asReversed()) }
            INDEX_OF -> OperatorIndexOf
            REMOVE -> OperatorRemove
            REPLACE -> OperatorReplace
            CONS -> OperatorBinary(pair(IotaPredicate.ofType(LIST), IotaPredicate.TRUE)) { list, iota -> ListIota(SpellList.LPair(iota, downcast(list, LIST).list)) }
            UNCONS -> OperatorUnCons
            else -> null
        }
    }
}
