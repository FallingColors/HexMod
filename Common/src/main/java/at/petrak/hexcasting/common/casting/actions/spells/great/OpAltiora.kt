package at.petrak.hexcasting.common.casting.actions.spells.great

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getPlayer
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.player.AltioraAbility
import at.petrak.hexcasting.common.lib.HexSounds
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.world.phys.Vec3
import kotlin.math.max

object OpAltiora : SpellAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val target = args.getPlayer(0, argc)
        env.assertEntityInRange(target)

        return SpellAction.Result(
            Spell(target),
            MediaConstants.CRYSTAL_UNIT,
            listOf(
                ParticleSpray.burst(target.position(), 0.5),
                ParticleSpray(target.position(), Vec3(0.0, 2.0, 0.0), 0.0, 0.1)
            )
        )
    }

    private data class Spell(val target: ServerPlayer) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            target.push(0.0, 1.5, 0.0)
            // They won't move otherwise?
            target.hurtMarked = true

            IXplatAbstractions.INSTANCE.setAltiora(target, AltioraAbility(GRACE_PERIOD))
        }
    }

    // TODO: this sends a packet to the player every tick. I need to find out what the monotonically increasing time value is
    @JvmStatic
    fun checkPlayerCollision(player: ServerPlayer) {
        val altiora = IXplatAbstractions.INSTANCE.getAltiora(player);
        if (altiora != null) {
            if (altiora.gracePeriod == 0 && (player.onGround() || player.horizontalCollision)) {
                IXplatAbstractions.INSTANCE.setAltiora(player, null)
                player.level().playSound(null, player.x, player.y, player.z, HexSounds.FLIGHT_FINISH, SoundSource.PLAYERS, 2f, 1f)
            } else {
                val grace2 = max(altiora.gracePeriod - 1, 0)
                IXplatAbstractions.INSTANCE.setAltiora(player, AltioraAbility(grace2))

                if (player.level().random.nextFloat() < 0.02)
                    player.level().playSound(null, player.x, player.y, player.z, HexSounds.FLIGHT_AMBIENCE, SoundSource.PLAYERS, 0.2f, 1f)

                val color = IXplatAbstractions.INSTANCE.getPigment(player)
                ParticleSpray(player.position(), Vec3(0.0, -0.2, 0.0), 0.4, Math.PI * 0.5, count = 3)
                    .sprayParticles(player.serverLevel(), color)
            }
        }
    }

    fun checkAllPlayers(world: ServerLevel) {
        for (player in world.players()) {
            checkPlayerCollision(player)
        }
    }

    private val GRACE_PERIOD = 20
}