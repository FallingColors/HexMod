package at.petrak.hexcasting.interop.pehkui

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpGetScale : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val target = args.getEntity(env.world, 0, argc)
        env.assertEntityInRange(target)
        return IXplatAbstractions.INSTANCE.pehkuiApi.getScale(target).toDouble().asActionResult
    }
}
