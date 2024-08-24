package at.petrak.hexcasting.common.casting.actions.stack

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getPositiveInt
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes

object OpDuplicateN : ConstMediaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        var count = args.getPositiveInt(1, argc)

        if (count > HexIotaTypes.MAX_SERIALIZATION_TOTAL) {
            // If we throw here, the message will point to us, which usually doesn't happen.
            // So ensure that this check has no user-facing effects, just cap to MAX_SERIALIZATION_TOTAL,
            // which will unconditionally trigger Too Many Iotas after we return.
            count = HexIotaTypes.MAX_SERIALIZATION_TOTAL
        }

        return (List(count) { args[0] })
    }
}
