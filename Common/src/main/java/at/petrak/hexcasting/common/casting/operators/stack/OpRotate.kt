package at.petrak.hexcasting.common.casting.operators.stack

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota

// more Forth
object OpRotate : ConstMediaAction {
    override val argc: Int
        get() = 3

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> = listOf(args[1], args[2], args[0])
}
