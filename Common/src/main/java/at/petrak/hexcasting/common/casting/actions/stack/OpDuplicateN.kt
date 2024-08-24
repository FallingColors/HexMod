package at.petrak.hexcasting.common.casting.actions.stack

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getPositiveInt
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapStackSize
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes

object OpDuplicateN : ConstMediaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val count = args.getPositiveInt(1, argc)

        if (count > HexIotaTypes.MAX_SERIALIZATION_TOTAL) {
            throw MishapStackSize()
        }

        return (List(count) { args[0] })
    }
}
