package at.petrak.hexcasting.common.casting

import at.petrak.hexcasting.api.RenderedSpell
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.common.particles.HexParticles
import at.petrak.hexcasting.datagen.Advancements
import com.mojang.math.Quaternion
import com.mojang.math.Vector3f
import net.minecraft.Util
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * Things that happen after a spell is cast.
 */
sealed class OperatorSideEffect {
    /** Return whether to cancel all further [OperatorSideEffect] */
    abstract fun performEffect(harness: CastingHarness): Boolean

    /** Try to cast a spell  */
    data class AttemptSpell(val spell: RenderedSpell, val isGreat: Boolean) : OperatorSideEffect() {
        override fun performEffect(harness: CastingHarness): Boolean {
            return if (this.isGreat && !harness.ctx.isCasterEnlightened) {
                harness.ctx.caster.sendMessage(
                    TranslatableComponent("hexcasting.message.cant_great_spell"),
                    Util.NIL_UUID
                )
                Advancements.FAIL_GREAT_SPELL_TRIGGER.trigger(harness.ctx.caster)
                true
            } else {
                this.spell.cast(harness.ctx)
                false
            }
        }
    }

    data class ConsumeMana(val amount: Int) : OperatorSideEffect() {
        override fun performEffect(harness: CastingHarness): Boolean {
            val overcastOk = harness.ctx.canOvercast
            val leftoverMana = harness.withdrawMana(this.amount, overcastOk)
            if (leftoverMana > 0 && overcastOk) {
                harness.ctx.caster.sendMessage(
                    TranslatableComponent("hexcasting.message.cant_overcast"),
                    Util.NIL_UUID
                )
            }
            return leftoverMana > 0
        }
    }

    data class Particles(val position: Vec3, val velocity: Vec3, val fuzziness: Double, val spread: Double) :
        OperatorSideEffect() {
        override fun performEffect(harness: CastingHarness): Boolean {
            val colorizer = harness.getColorizer()

            for (i in 0 until 20) {
                // For the colors, pick any random time to get a mix of colors
                val color = colorizer.getColor(Random.nextFloat() * 256f, Vec3.ZERO)

                // https://math.stackexchange.com/questions/44689/how-to-find-a-random-axis-or-unit-vector-in-3d
                fun randomInCircle(maxTh: Double = Mth.TWO_PI.toDouble()): Vec3 {
                    val th = Random.nextDouble(0.0, maxTh)
                    val z = Random.nextDouble(-1.0, 1.0)
                    return Vec3(sqrt(1.0 - z * z) * cos(th), sqrt(1.0 - z * z) * sin(th), z)
                }

                val offset = randomInCircle().scale(fuzziness)
                val pos = position.add(offset)

                // https://math.stackexchange.com/questions/56784/generate-a-random-direction-within-a-cone
                val northCone = randomInCircle(spread)
                val velNorm = velocity.normalize()
                val zp = Vec3(0.0, 0.0, 1.0)
                val rotAxis = velNorm.cross(zp)
                val th = acos(velNorm.dot(zp))
                val dagn = Quaternion(Vector3f(rotAxis), th.toFloat(), false)
                val velf = Vector3f(northCone)
                velf.transform(dagn)
                val vel = Vec3(velf).scale(velocity.length())

                // TODO: this doesn't work because xyz velocity is a lie
                harness.ctx.world.addParticle(
                    HexParticles.CONJURE_BLOCK_PARTICLE.get(),
                    pos.x, pos.y, pos.z,
                    vel.x, vel.y, vel.z,
                )
            }

            return false
        }
    }

    data class Mishap(val exn: CastException) : OperatorSideEffect() {
        override fun performEffect(harness: CastingHarness): Boolean {
            harness.ctx.caster.sendMessage(
                TextComponent(exn.toString()),
                Util.NIL_UUID
            )

            when (exn.reason) {
                CastException.Reason.INVALID_PATTERN -> {
                    val idx = Random.nextInt(0..harness.stack.size)
                    harness.stack.add(idx, SpellDatum.make(Widget.GARBAGE))
                }
                else -> {}
            }

            return false
        }
    }
}