package at.petrak.hexcasting.common.casting.operators.stack

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota

// Had never heard of this operation before i heard it from uxn, but turns out it's from forth
// see also http://www.forth.org/Ting/Forth-for-the-Complete-Idiot/Forth-79-Handy-Reference.pdf
// "fisherman's" is called "roll," apparently
object OpOver : ConstMediaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> = listOf(args[0], args[1], args[0])
}
