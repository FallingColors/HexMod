package at.petrak.hexcasting.common.casting.operators.rw

import at.petrak.hexcasting.api.casting.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpTheCoolerReadable : ConstMediaAction {
    override val argc = 1

    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): List<Iota> {
        val target = args.getEntity(0, argc)
        ctx.assertEntityInRange(target)

        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(target)
            ?: return false.asActionResult

        datumHolder.readIota(ctx.world)
            ?: datumHolder.emptyIota()
            ?: return false.asActionResult

        return true.asActionResult
    }
}
