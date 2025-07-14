package at.petrak.hexcasting.common.casting.actions.lists

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getInt
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.utils.Vector

object OpRemove : ConstMediaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: Vector<Iota>, env: CastingEnvironment): Vector<Iota> {
        val list = args.getList(0, argc)
        val index = args.getInt(1, argc)
        if (index < 0 || index >= list.size)
            return list.asActionResult
        list.removeAt(index)
        return list.asActionResult
    }
}
