package at.petrak.hexcasting.common.casting.actions.queryentity

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getPlayer
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpCanEntityHexFly : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val player = args.getPlayer(env.world, 0, argc)
        env.assertEntityInRange(player)

        val flightAbility = IXplatAbstractions.INSTANCE.getFlight(player)
        return (flightAbility != null).asActionResult
    }
}
