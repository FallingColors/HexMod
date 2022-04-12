package at.petrak.hexcasting.common.casting.operators.spells.great

import at.petrak.hexcasting.api.player.FlightAbility
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.player.HexPlayerDataHelper
import at.petrak.hexcasting.common.network.HexMessages
import at.petrak.hexcasting.common.network.MsgAddMotionAck
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.network.PacketDistributor
import kotlin.math.max
import kotlin.math.roundToInt

object OpFlight : SpellOperator {
    override val argc = 3
    override val isGreat = true
    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getChecked<ServerPlayer>(0)
        val timeRaw = max(args.getChecked(1), 0.0)
        val radiusRaw = max(args.getChecked(2), 0.0)
        ctx.assertEntityInRange(target)

        // Convert to ticks
        val time = (timeRaw * 20.0).roundToInt()
        return Triple(
            Spell(target, time, radiusRaw, ctx.position),
            10_000 * (timeRaw * radiusRaw + 1.0).roundToInt(),
            listOf(ParticleSpray(target.position(), Vec3(0.0, 2.0, 0.0), 0.0, 0.1))
        )
    }

    data class Spell(val target: ServerPlayer, val time: Int, val radius: Double, val origin: Vec3) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            if (target.abilities.mayfly) {
                // Don't accidentally clobber someone else's flight
                return
            }

            HexPlayerDataHelper.setFlight(target,
                FlightAbility(
                    true,
                    time,
                    target.level.dimension(),
                    origin,
                    radius
                )
            )

            target.abilities.mayfly = true
            target.abilities.flying = true
            target.onUpdateAbilities()
            // Launch the player into the air to really emphasize the flight
            HexMessages.getNetwork()
                .send(PacketDistributor.PLAYER.with { target }, MsgAddMotionAck(Vec3(0.0, 1.0, 0.0)))
        }
    }

    @SubscribeEvent
    fun tickDownFlight(evt: LivingEvent.LivingUpdateEvent) {
        val entity = evt.entityLiving
        if (entity !is ServerPlayer) return

        val flight = HexPlayerDataHelper.getFlight(entity)

        if (flight.allowed) {
            val flightTime = flight.timeLeft - 1
            if (flightTime < 0 || flight.origin.distanceToSqr(entity.position()) > flight.radius * flight.radius || flight.dimension != entity.level.dimension()) {
                if (!entity.isOnGround) {
                    entity.fallDistance = 1_000_000f
                }
                HexPlayerDataHelper.setFlight(entity, FlightAbility.deny())

                if (!entity.isCreative && !entity.isSpectator) {
                    val abilities = entity.abilities
                    abilities.flying = false
                    abilities.mayfly = false
                    entity.onUpdateAbilities()
                }
            } else
                HexPlayerDataHelper.setFlight(entity,
                    FlightAbility(
                        true,
                        flightTime,
                        flight.dimension,
                        flight.origin,
                        flight.radius
                    )
                )
        }

    }
}
