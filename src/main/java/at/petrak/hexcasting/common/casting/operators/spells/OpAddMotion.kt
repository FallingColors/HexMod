package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.common.network.HexMessages
import at.petrak.hexcasting.common.network.MsgAddMotionAck
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import net.minecraftforge.network.PacketDistributor

object OpAddMotion : SpellOperator {
    override val argc: Int
        get() = 2

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getChecked<Entity>(0)
        val motion = args.getChecked<Vec3>(1)
        ctx.assertEntityInRange(target)
        var motionForCost = motion.lengthSqr()
        if (ctx.hasBeenGivenMotion(target))
            motionForCost++
        return Triple(
            Spell(target, motion),
            (motionForCost * 10_000f).toInt(),
            listOf(
                ParticleSpray(
                    target.position().add(0.0, target.eyeHeight / 2.0, 0.0),
                    motion.normalize(),
                    0.0,
                    0.1
                )
            ),
        )
    }

    private data class Spell(val target: Entity, val motion: Vec3) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            if (target is ServerPlayer) {
                // Player movement is apparently handled on the client; who knew
                // There's apparently some magic flag I can set to auto-sync it but I can't find it
                HexMessages.getNetwork().send(PacketDistributor.PLAYER.with { target }, MsgAddMotionAck(motion))
            }
            target.deltaMovement = target.deltaMovement.add(motion)
            ctx.markEntityAsMotionAdded(target)
        }
    }
}
