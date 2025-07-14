package at.petrak.hexcasting.common.casting.actions.lists

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.utils.Vector

object OpSplat : ConstMediaAction {
    override val argc: Int
        get() = 1

    override fun execute(args: Vector<Iota>, env: CastingEnvironment): Vector<Iota> =
        args.getList(0, argc)
}
