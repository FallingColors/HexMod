package at.petrak.hexcasting.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getPlayer
import at.petrak.hexcasting.api.casting.getPositiveDouble
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.player.FlightAbility
import at.petrak.hexcasting.common.lib.HexItems
import at.petrak.hexcasting.common.lib.HexSounds
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.Util
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.util.Mth
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class OpFlight(val type: Type) : SpellAction {
    override val argc = 2
    override fun execute(
            args: List<Iota>,
            env: CastingEnvironment
    ): SpellAction.Result {
        val target = args.getPlayer(0, argc)
        val theArg = args.getPositiveDouble(1, argc)
        env.assertEntityInRange(target)

        val cost = when (this.type) {
            Type.LimitRange -> theArg * MediaConstants.DUST_UNIT
            // A second of flight should cost 1 shard
            Type.LimitTime -> theArg * MediaConstants.SHARD_UNIT
        }.roundToLong()

        // Convert to ticks
        return SpellAction.Result(
            Spell(this.type, target, theArg),
            cost,
            listOf(ParticleSpray(target.position(), Vec3(0.0, 2.0, 0.0), 0.0, 0.1))
        )
    }

    enum class Type {
        LimitRange,
        LimitTime;

    }

    data class Spell(val type: Type, val target: ServerPlayer, val theArg: Double) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            if (target.abilities.mayfly) {
                // Don't accidentally clobber someone else's flight
                // TODO make this a mishap?
                return
            }

            val dim = target.level().dimension()
            val origin = target.position()

            val flight = when (this.type) {
                Type.LimitRange -> FlightAbility(-1, dim, origin, theArg)
                Type.LimitTime -> FlightAbility((theArg * 20.0).roundToInt(), dim, origin, -1.0)
            }

            IXplatAbstractions.INSTANCE.setFlight(target, flight)

            target.abilities.mayfly = true
            target.onUpdateAbilities()
        }
    }


    companion object {
        // blocks from the edge
        private val DIST_DANGER_THRESHOLD = 4.0

        // seconds left
        private val TIME_DANGER_THRESHOLD = 7.0 * 20.0

        @JvmStatic
        fun tickAllPlayers(world: ServerLevel) {
            for (player in world.players()) {
                tickDownFlight(player)
            }
        }

        @JvmStatic
        fun tickDownFlight(player: ServerPlayer) {
            val flight = IXplatAbstractions.INSTANCE.getFlight(player)

            if (flight != null) {
                val danger = getDanger(player, flight)
                if (danger >= 1.0) {
                    IXplatAbstractions.INSTANCE.setFlight(player, null)
                    // stop shin smashing bonke

                    if (!player.isCreative && !player.isSpectator) {
                        val abilities = player.abilities
                        abilities.flying = false
                        abilities.mayfly = false
                        player.onUpdateAbilities()
                    }
                    player.level().playSound(null, player.x, player.y, player.z, HexSounds.FLIGHT_FINISH, SoundSource.PLAYERS, 2f, 1f)
                    val superDangerSpray = ParticleSpray(player.position(), Vec3(0.0, 1.0, 0.0), Math.PI, 0.4, count = 20)
                    superDangerSpray.sprayParticles(player.serverLevel(), FrozenPigment(ItemStack(HexItems.DYE_PIGMENTS[DyeColor.RED]!!), Util.NIL_UUID))
                    superDangerSpray.sprayParticles(player.serverLevel(), FrozenPigment(ItemStack(HexItems.DYE_PIGMENTS[DyeColor.BLACK]!!), Util.NIL_UUID))
                } else {
                    if (!player.abilities.mayfly) {
                        player.abilities.mayfly = true
                        player.onUpdateAbilities()
                    }
                    val time2 = if (flight.timeLeft >= 0) {
                        flight.timeLeft - 1
                    } else {
                        flight.timeLeft
                    }
                    IXplatAbstractions.INSTANCE.setFlight(
                        player,
                        FlightAbility(
                            time2,
                            flight.dimension,
                            flight.origin,
                            flight.radius
                        )
                    )

                    val particleCount = 5
                    val dangerParticleCount = (particleCount * danger).roundToInt()
                    val okParticleCount = particleCount - dangerParticleCount
                    val oneDangerParticleCount = Mth.ceil(dangerParticleCount / 2.0)
                    val color = IXplatAbstractions.INSTANCE.getPigment(player)

                    // TODO: have the particles go in the opposite direction of the velocity?
                    ParticleSpray(player.position(), Vec3(0.0, -0.6, 0.0), 0.6, Math.PI * 0.3, count = okParticleCount)
                        .sprayParticles(player.serverLevel(), color)
                    val dangerSpray = ParticleSpray(player.position(), Vec3(0.0, 1.0, 0.0), 0.3, Math.PI * 0.75, count = 0)
                    dangerSpray.copy(count = oneDangerParticleCount)
                        .sprayParticles(player.serverLevel(), FrozenPigment(ItemStack(HexItems.DYE_PIGMENTS[DyeColor.BLACK]!!), Util.NIL_UUID))
                    dangerSpray.copy(count = oneDangerParticleCount)
                        .sprayParticles(player.serverLevel(), FrozenPigment(ItemStack(HexItems.DYE_PIGMENTS[DyeColor.RED]!!), Util.NIL_UUID))

                    if (player.level().random.nextFloat() < 0.02)
                        player.level().playSound(null, player.x, player.y, player.z, HexSounds.FLIGHT_AMBIENCE, SoundSource.PLAYERS, 0.2f, 1f)

                    if (flight.radius >= 0.0) {
                        // Show the origin
                        val spoofedOrigin = flight.origin.add(0.0, 1.0, 0.0)
                        ParticleSpray(spoofedOrigin, Vec3(0.0, 1.0, 0.0), 0.5, Math.PI * 0.1, count = 5)
                            .sprayParticles(player.serverLevel(), color)
                        ParticleSpray(spoofedOrigin, Vec3(0.0, -1.0, 0.0), 1.5, Math.PI * 0.25, count = 5)
                            .sprayParticles(player.serverLevel(), color)
                    }
                }
            }
        }

        // Return a number from 0 (totally fine) to 1 (danger will robinson, stop the flight)
        // it's a double for particle reason
        private fun getDanger(player: ServerPlayer, flight: FlightAbility): Double {
            val radiusDanger = if (flight.radius >= 0.0) {
                if (player.level().dimension() != flight.dimension) {
                    1.0
                } else {
                    // Limit it only in X/Z
                    val posXZ = Vec3(player.x, 0.0, player.z)
                    val originXZ = Vec3(flight.origin.x, 0.0, flight.origin.z)
                    val dist = posXZ.distanceTo(originXZ)
                    val distFromEdge = flight.radius - dist
                    if (distFromEdge >= DIST_DANGER_THRESHOLD) {
                        0.0
                    } else if (dist > flight.radius) {
                        1.0
                    } else {
                        1.0 - (distFromEdge / DIST_DANGER_THRESHOLD)
                    }
                }
            } else 0.0
            val timeDanger = if (flight.timeLeft >= 0) {
                if (flight.timeLeft >= TIME_DANGER_THRESHOLD) {
                    0.0
                } else {
                    val timeDanger = TIME_DANGER_THRESHOLD - flight.timeLeft
                    timeDanger / TIME_DANGER_THRESHOLD
                }
            } else 0.0
            return max(radiusDanger, timeDanger)
        }
    }
}
