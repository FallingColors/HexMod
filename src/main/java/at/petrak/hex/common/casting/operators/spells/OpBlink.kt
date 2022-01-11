package at.petrak.hex.common.casting.operators.spells

import at.petrak.hex.api.Operator.Companion.getChecked
import at.petrak.hex.api.RenderedSpell
import at.petrak.hex.api.SpellDatum
import at.petrak.hex.api.SpellOperator
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.network.HexMessages
import at.petrak.hex.common.network.MsgBlinkAck
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import net.minecraftforge.network.PacketDistributor
import kotlin.math.max
import kotlin.math.roundToInt

object OpBlink : SpellOperator {
    override val argc = 2
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Pair<RenderedSpell, Int> {
        val target = args.getChecked<Entity>(0)
        val delta = args.getChecked<Double>(1)

        ctx.assertVecInRange(target.position())

        return Pair(
            Spell(target, delta),
            1_000 * (delta * delta).roundToInt(),
        )
    }

    class Spell(val target: Entity, val delta: Double) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val look = target.lookAngle
            // https://github.com/VazkiiMods/Psi/blob/master/src/main/java/vazkii/psi/common/spell/trick/entity/PieceTrickBlink.java#L74
            // IIRC this is to prevent you from teleporting into blocks because people tend to look a little bit down
            // ... but isn't the condition backwards?
            val dx = look.x * delta
            val dy = if (target == ctx.caster) {
                look.y * delta
            } else {
                max(0.0, look.y * delta)
            }
            val dz = look.z * delta

            val dvec = Vec3(dx, dy, dz)
            target.setPos(target.position().add(dvec))
            if (target is ServerPlayer) {
                HexMessages.getNetwork().send(PacketDistributor.PLAYER.with { target }, MsgBlinkAck(dvec))
            }
        }
    }
}