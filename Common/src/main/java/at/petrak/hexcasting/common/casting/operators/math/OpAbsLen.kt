package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getNumOrVec
import at.petrak.hexcasting.api.spell.iota.Iota
import kotlin.math.absoluteValue

object OpAbsLen : ConstManaAction {
    override val argc: Int
        get() = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val x = args.getNumOrVec(0, argc)

        return x.map({ num -> num.absoluteValue }, { vec -> vec.length() }).asActionResult
    }
}
