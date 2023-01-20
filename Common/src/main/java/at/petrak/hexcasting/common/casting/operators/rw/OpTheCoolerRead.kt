package at.petrak.hexcasting.common.casting.operators.rw

import at.petrak.hexcasting.api.casting.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadEntity
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpTheCoolerRead : ConstMediaAction {
    override val argc = 1

    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): List<Iota> {
        val target = args.getEntity(0, argc)

        ctx.assertEntityInRange(target)

        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(target)
            ?: throw MishapBadEntity.of(target, "iota.read")

        val datum = datumHolder.readIota(ctx.world)
            ?: datumHolder.emptyIota()
            ?: throw MishapBadEntity.of(target, "iota.read")
        return listOf(datum)
    }
}
