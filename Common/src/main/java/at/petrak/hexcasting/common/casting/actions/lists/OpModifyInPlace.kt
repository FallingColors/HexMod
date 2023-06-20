package at.petrak.hexcasting.common.casting.actions.lists

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.getPositiveIntUnder
import at.petrak.hexcasting.api.casting.iota.Iota

object OpModifyInPlace : ConstMediaAction {
    override val argc = 3
    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val list = args.getList(0, argc)
        val index = args.getPositiveIntUnder(1, list.size(), argc)
        val iota = args[2]
        return list.modifyAt(index) { SpellList.LPair(iota, it.cdr) }.asActionResult
    }
}
