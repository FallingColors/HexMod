package at.petrak.hexcasting.common.casting.actions.lists

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.utils.Vector

object OpEmptyList : ConstMediaAction {
    override val argc = 0
    override fun execute(args: Vector<Iota>, env: CastingEnvironment): Vector<Iota> {
        return Vector.empty<Iota>().asActionResult // sorry for taking all the easy impls, hudeler
    }
}
