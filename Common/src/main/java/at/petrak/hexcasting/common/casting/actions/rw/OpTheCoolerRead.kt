package at.petrak.hexcasting.common.casting.actions.rw

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntityOrBlockPos
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBad
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpTheCoolerRead : ConstMediaAction {
    override val argc = 1

    override fun execute(
        args: List<Iota>,
        env: CastingEnvironment
    ): List<Iota> {
        val target = args.getEntityOrBlockPos(0, argc)

        target.map(env::assertEntityInRange, env::assertPosInRange)

        val datumHolder = target.map(
            IXplatAbstractions.INSTANCE::findDataHolder,
            {pos -> IXplatAbstractions.INSTANCE.findDataHolder(pos, env.world)})
            ?: throw MishapBad.of(target, "iota.read")

        val datum = datumHolder.readIota(env.world)
            ?: datumHolder.emptyIota()
            ?: throw MishapBad.of(target, "iota.read")
        return listOf(datum)
    }
}
