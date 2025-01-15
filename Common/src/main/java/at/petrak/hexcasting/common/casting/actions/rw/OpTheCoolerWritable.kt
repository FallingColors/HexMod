package at.petrak.hexcasting.common.casting.actions.rw

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntityOrBlockPos
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpTheCoolerWritable : ConstMediaAction {
    override val argc = 1

    override fun execute(
        args: List<Iota>,
        env: CastingEnvironment
    ): List<Iota> {
        val target = args.getEntityOrBlockPos(0, argc)

        target.map(env::assertEntityInRange, env::assertPosInRangeForEditing)

        val datumHolder = target.map(
            IXplatAbstractions.INSTANCE::findDataHolder,
            {pos -> IXplatAbstractions.INSTANCE.findDataHolder(pos, env.world)})
            ?: return false.asActionResult

        val success = datumHolder.writeIota(NullIota(), true)

        return success.asActionResult
    }
}
