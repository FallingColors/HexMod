package at.petrak.hexcasting.common.casting.actions.stack

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getPositiveInt
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.mod.HexConfig

object OpDuplicateN : ConstMediaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        var count = args.getPositiveInt(1, argc)

        if (count > HexConfig.server().maxStackSize()) {
            // If we throw here, the message will point to us, which usually doesn't happen.
            // So ensure that this check has no user-facing effects, just cap to maxStackSize plus 1,
            // which will unconditionally trigger Too Many Iotas after we return.
            count = HexConfig.server().maxStackSize() + 1
        }

        return (List(count) { args[0] })
    }
}
