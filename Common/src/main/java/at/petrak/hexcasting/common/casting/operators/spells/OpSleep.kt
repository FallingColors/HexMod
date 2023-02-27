package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Mob

object OpSleep : SpellAction {
    override val argc = 1
    override fun execute(args: List<Iota>, ctx: CastingEnvironment): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
        val target = args.getEntity(0, argc)

        if (target is Mob) {
            val goals = IXplatAbstractions.INSTANCE.getGoalSelector(target)
            val targets = IXplatAbstractions.INSTANCE.getTargetSelector(target)

            goals.runningGoals.forEach { it.stop() }
            targets.runningGoals.forEach { it.stop() }
        } else if (target is ServerPlayer) {

        }
        TODO()
    }

    @JvmStatic
    fun getPostSleepTime(level: ServerLevel): Long {
        TODO()
    }
}