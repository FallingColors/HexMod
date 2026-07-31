package at.petrak.hexcasting.common.casting.actions.queryblock

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.iota.Iota

class OpBlockEquality(val exact: Boolean) : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val posA = args.getBlockPos(0).also { env.assertPosInRange(it) }
        val posB = args.getBlockPos(1).also { env.assertPosInRange(it) }

        val blockA = env.world.getBlockState(posA)
        val blockB = env.world.getBlockState(posB)

        // a unique BlockState only has one instance
        val matches = if (exact) blockA === blockB else blockA.`is`(blockB.block)

        return matches.asActionResult
    }
}
