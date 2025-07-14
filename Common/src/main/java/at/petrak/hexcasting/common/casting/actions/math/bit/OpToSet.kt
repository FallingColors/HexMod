package at.petrak.hexcasting.common.casting.actions.math.bit

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.utils.Vector

object OpToSet : ConstMediaAction {
    override val argc = 1

    override fun execute(args: Vector<Iota>, env: CastingEnvironment): Vector<Iota> {
        val list = args.getList(0, argc)
        val out = mutableListOf<Iota>()

        for (subiota in list) {
            if (out.none { Iota.tolerates(it, subiota) }) {
                out.add(subiota)
            }
        }

        return Vector.from(out).asActionResult
    }
}
