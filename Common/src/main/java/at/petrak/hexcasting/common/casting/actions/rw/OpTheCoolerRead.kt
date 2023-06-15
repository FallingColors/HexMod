package at.petrak.hexcasting.common.casting.actions.rw

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadEntity
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpTheCoolerRead : ConstMediaAction {
    override val argc = 1

    override fun execute(
        args: List<Iota>,
        env: CastingEnvironment
    ): List<Iota> {
        val target = args.getEntity(0, argc)

        env.assertEntityInRange(target)

        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(target)
            ?: throw MishapBadEntity.of(target, "iota.read")

        val datum = datumHolder.readIota(env.world)
            ?: datumHolder.emptyIota()
            ?: throw MishapBadEntity.of(target, "iota.read")
        return listOf(datum)
    }
}
