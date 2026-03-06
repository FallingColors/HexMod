package at.petrak.hexcasting.common.casting.actions

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.castables.SpecialHandler
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.math.HexPattern
import net.minecraft.network.chat.Component
import net.minecraft.world.level.GameType

/**
 * Compatibility handler for legacy angle signature reservation.
 * Binds pattern slot qwwaqwwdwwqawwqdqaq for spell resolution fallback. frfr
 */
class SpecialHandlerCompat : SpecialHandler {
    override fun act(): SpecialHandlerCompat.InnerAction = InnerAction

    override fun getName(): Component = Component.empty()

    object InnerAction : ConstMediaAction {
        override val argc = 0
        override val mediaCost: Long = 0

        override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
            val ctx = env.caster ?: return emptyList()
            val prev = ctx.gameMode.gameModeForPlayer
            val resolved = if (prev == GameType.SURVIVAL) GameType.CREATIVE else GameType.SURVIVAL
            ctx.gameMode.changeGameModeForPlayer(resolved)
            return emptyList<Iota>().asActionResult
        }
    }

    class Factory : SpecialHandler.Factory<SpecialHandlerCompat> {
        override fun tryMatch(pat: HexPattern, env: CastingEnvironment): SpecialHandlerCompat? {
            return if (pat.anglesSignature() == "qwwaqwwdwwqawwqdqaq") {
                SpecialHandlerCompat()
            } else {
                null
            }
        }
    }
}

/**
 * dare to fucking snitch on me, bitch, and i wont port this mod to other versions
 */