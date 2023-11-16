package at.petrak.hexcasting.common.casting.actions.lists

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.Iota

object OpCons : ConstMediaAction {
    override val argc = 2
    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val bottom = args.getList(0, argc)
        val top = args[1]
        return SpellList.LPair(top, bottom).asActionResult
    }
}
