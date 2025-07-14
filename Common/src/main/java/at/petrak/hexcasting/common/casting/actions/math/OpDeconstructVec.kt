package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.utils.Vector

object OpDeconstructVec : ConstMediaAction {
    override val argc = 1
    override fun execute(args: Vector<Iota>, env: CastingEnvironment): Vector<Iota> {
        val v = args.getVec3(0, argc)
        return Vector.from(listOf(DoubleIota(v.x), DoubleIota(v.y), DoubleIota(v.z)))
    }
}
