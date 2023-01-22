package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment

object OpEmptyList : ConstMediaAction {
    override val argc = 0
    override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
        return emptyList<Iota>().asActionResult // sorry for taking all the easy impls, hudeler
    }
}
