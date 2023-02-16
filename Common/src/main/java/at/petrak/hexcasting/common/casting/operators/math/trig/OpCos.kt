package at.petrak.hexcasting.common.casting.operators.math.trig

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.iota.Iota
import kotlin.math.cos

object OpCos : ConstMediaAction {
    override val argc: Int
        get() = 1

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
        val angle = args.getDouble(0, argc)
        return cos(angle).asActionResult
    }
}
