package at.petrak.hexcasting.common.casting.operators.stack

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getPositiveInt
import at.petrak.hexcasting.api.spell.iota.Iota

object OpDuplicateN : ConstManaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val count = args.getPositiveInt(1, argc)

        return (List(count) { args[0] })
    }
}
