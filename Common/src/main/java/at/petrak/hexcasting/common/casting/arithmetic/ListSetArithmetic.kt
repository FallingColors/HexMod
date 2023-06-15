package at.petrak.hexcasting.common.casting.arithmetic

import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic
import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic.*
import at.petrak.hexcasting.api.casting.arithmetic.engine.InvalidOperatorException
import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator
import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator.*
import at.petrak.hexcasting.api.casting.arithmetic.operator.OperatorBinary
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate.*
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.casting.arithmetic.operator.list.OperatorUnique
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes.LIST
import java.util.function.BinaryOperator

object ListSetArithmetic : Arithmetic {
    private val OPS = listOf(
        AND,
        OR,
        XOR,
        UNIQUE
    )

    override fun arithName() = "list_set_ops"

    override fun opTypes() = OPS

    override fun getOperator(pattern: HexPattern): Operator = when (pattern) {
        AND -> make2 { list0, list1 -> list0.filter { x -> list1.any { Iota.tolerates(x, it) } } }
        OR -> make2 { list0, list1 -> list0 + list1.filter { x -> list0.none { Iota.tolerates(x, it) } } }
        XOR -> make2 { list0, list1 -> list0.filter { x0 -> list1.none {Iota.tolerates(x0, it) } } + list1.filter { x1 -> list0.none { Iota.tolerates(x1, it) } } }
        UNIQUE -> OperatorUnique
        else -> throw InvalidOperatorException("$pattern is not a valid operator in Arithmetic $this.")
    }


    private fun make2(op: BinaryOperator<List<Iota>>): OperatorBinary = OperatorBinary(all(IotaPredicate.ofType(LIST)))
    { i: Iota, j: Iota -> ListIota(
            op.apply(downcast(i, LIST).list.toList(), downcast(j, LIST).list.toList())
        ) }
}