package at.petrak.hexcasting.common.casting.actions.lists

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.utils.Vector

object OpUnCons : ConstMediaAction {
    override val argc = 1
    override fun execute(args: Vector<Iota>, env: CastingEnvironment): Vector<Iota> {
        val list = args.getList(0, argc)
        if (!list.isEmpty()) {
            return Vector.from(listOf(ListIota(list.tail()), list.head()))
        }
        return Vector.from(listOf(args[0], NullIota()))
    }
}
