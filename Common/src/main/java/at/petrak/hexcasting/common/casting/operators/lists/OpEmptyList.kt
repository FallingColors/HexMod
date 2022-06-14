package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext

object OpEmptyList : ConstManaAction {
    override val argc = 0
    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        return emptyList<Iota>().asActionResult // sorry for taking all the easy impls, hudeler
    }
}
