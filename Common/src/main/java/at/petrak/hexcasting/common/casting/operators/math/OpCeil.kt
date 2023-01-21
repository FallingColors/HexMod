package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.aplKinnie
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.getNumOrVec
import at.petrak.hexcasting.api.casting.iota.Iota
import kotlin.math.ceil

object OpCeil : ConstMediaAction {
    override val argc: Int
        get() = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val value = args.getNumOrVec(0, argc)
        // i hate this fucking syntax what the hell is ::ceil are you a goddamn homestuck ::c
        return listOf(aplKinnie(value, ::ceil))
    }
}
