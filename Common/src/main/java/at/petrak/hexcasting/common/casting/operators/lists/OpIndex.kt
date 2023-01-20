package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.casting.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import kotlin.math.roundToInt

object OpIndex : ConstMediaAction {
    override val argc = 2
    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val list = args.getList(0, argc).toMutableList()
        val index = args.getDouble(1, argc)
        val x = list.getOrElse(index.roundToInt()) { NullIota() }
        return listOf(x)
    }
}
