package at.petrak.hexcasting.common.casting.actions.lists

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.utils.Vector
import kotlin.math.roundToInt

object OpIndex : ConstMediaAction {
    override val argc = 2
    override fun execute(args: Vector<Iota>, env: CastingEnvironment): Vector<Iota> {
        val list = args.getList(0, argc)
        val index = args.getDouble(1, argc)
        val x = list.getOrElse(index.roundToInt()) { NullIota() }
        return Vector.from(listOf(x))
    }
}
