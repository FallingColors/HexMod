package at.petrak.hex.common.casting.operators.spells.great

import at.petrak.hex.HexUtils
import at.petrak.hex.HexUtils.serializeToNBT
import at.petrak.hex.api.Operator.Companion.MAX_DISTANCE
import at.petrak.hex.api.Operator.Companion.getChecked
import at.petrak.hex.api.RenderedSpell
import at.petrak.hex.api.SpellDatum
import at.petrak.hex.api.SpellOperator
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.lib.LibCapabilities
import at.petrak.hex.common.network.HexMessages
import at.petrak.hex.common.network.MsgAddMotionAck
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilitySerializable
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.network.PacketDistributor
import kotlin.math.max
import kotlin.math.roundToInt

object OpFlight : SpellOperator {
    override val argc = 3
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Pair<RenderedSpell, Int> {
        val target = args.getChecked<ServerPlayer>(0)
        val timeRaw = max(args.getChecked(1), 0.0)
        val radiusRaw = max(args.getChecked(2), 0.0)

        // Convert to ticks
        val time = (timeRaw * 20.0).roundToInt()
        return Pair(
            Spell(target, time, radiusRaw + MAX_DISTANCE, ctx.position),
            1_000 * (timeRaw * radiusRaw + 1.0).roundToInt()
        )
    }

    data class Spell(val target: ServerPlayer, val time: Int, val radius: Double, val origin: Vec3) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val maybeCap = target.getCapability(LibCapabilities.FLIGHT).resolve()
            if (!maybeCap.isPresent) {
                // uh oh
                return
            }
            val cap = maybeCap.get()
            cap.allowed = true
            cap.flightTime = time
            cap.radius = radius
            cap.origin = origin
        }
    }

    const val CAP_NAME = "flight"
    const val TAG_ALLOWED = "can_fly"
    const val TAG_FLIGHT_TIME = "flight_time"
    const val TAG_ORIGIN = "origin"
    const val TAG_RADIUS = "radius"

    val dummyInstanceIHateForge: LazyOptional<CapFlight> =
        LazyOptional.of { CapFlight(false, 0, Vec3.ZERO, 0.0) }

    class CapFlight(var allowed: Boolean, var flightTime: Int, var origin: Vec3, var radius: Double) :
        ICapabilitySerializable<CompoundTag> {
        override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> =
            LibCapabilities.FLIGHT.orEmpty(cap, dummyInstanceIHateForge)

        override fun serializeNBT(): CompoundTag {
            val out = CompoundTag()
            out.putBoolean(TAG_ALLOWED, this.allowed)
            if (this.allowed) {
                out.putInt(TAG_FLIGHT_TIME, flightTime)
                out.put(TAG_ORIGIN, this.origin.serializeToNBT())
                out.putDouble(TAG_RADIUS, this.radius)
            }
            return out
        }

        override fun deserializeNBT(nbt: CompoundTag) {
            this.allowed = nbt.getBoolean(TAG_ALLOWED)
            if (this.allowed) {
                this.flightTime = nbt.getInt(TAG_FLIGHT_TIME)
                this.origin = HexUtils.DeserializeVec3FromNBT(nbt.getLongArray(TAG_ORIGIN))
                this.radius = nbt.getDouble(TAG_RADIUS)
            }
        }
    }

    @SubscribeEvent
    fun tickDownFlight(evt: LivingEvent.LivingUpdateEvent) {
        val entity = evt.entityLiving
        if (entity !is ServerPlayer) return
        val maybeCap = entity.getCapability(LibCapabilities.FLIGHT).resolve()
        if (!maybeCap.isPresent) {
            // nah we were just capping
            return
        }
        val cap = maybeCap.get()

        if (cap.allowed) {
            cap.flightTime--
            if (cap.flightTime < 0 || cap.origin.distanceToSqr(entity.position()) > cap.radius * cap.radius) {
                if (!entity.isOnGround) {
                    entity.fallDistance = 1_000_000f
                    val move = entity.deltaMovement
                    HexMessages.getNetwork()
                        .send(PacketDistributor.PLAYER.with { entity }, MsgAddMotionAck(Vec3(move.x, -100.0, move.z)))
                }
                cap.allowed = false
            }
        }
        if (!entity.isCreative && !entity.isSpectator) {
            val abilities = entity.abilities
            if (!cap.allowed) abilities.flying = false
            abilities.mayfly = cap.allowed
            entity.onUpdateAbilities()
        }
    }
}