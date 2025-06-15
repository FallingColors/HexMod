package at.petrak.hexcasting.common.casting.actions.rw

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpTheCoolerWritable : ConstMediaAction {
    override val argc = 1

    override fun execute(
        args: List<Iota>,
        env: CastingEnvironment
    ): List<Iota> {
        val target = args.getEntity(env.world, 0, argc)
        env.assertEntityInRange(target)

        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(target)
            ?: return false.asActionResult

        val success = datumHolder.writeIota(NullIota.INSTANCE, true)

        return success.asActionResult
    }
}
