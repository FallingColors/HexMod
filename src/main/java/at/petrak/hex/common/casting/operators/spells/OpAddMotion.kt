package at.petrak.hex.common.casting.operators.spells

import at.petrak.hex.api.SimpleOperator
import at.petrak.hex.api.SpellOperator.Companion.getChecked
import at.petrak.hex.api.SpellOperator.Companion.spellListOf
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.RenderedSpell
import at.petrak.hex.common.casting.RenderedSpellImpl
import at.petrak.hex.common.casting.SpellDatum
import at.petrak.hex.common.network.HexMessages
import at.petrak.hex.common.network.MsgAddMotionAck
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import net.minecraftforge.network.PacketDistributor

object OpAddMotion : SimpleOperator, RenderedSpellImpl {
    override val argc: Int
        get() = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val target = args.getChecked<Entity>(0)
        val motion = args.getChecked<Vec3>(1)
        return spellListOf(RenderedSpell(OpAddMotion, spellListOf(target, motion)))
    }

    override fun cast(args: List<SpellDatum<*>>, ctx: CastingContext) {
        val target = args.getChecked<Entity>(0)
        val motion = args.getChecked<Vec3>(1)

        if (target is ServerPlayer) {
            // Player movement is apparently handled on the client; who knew
            // There's apparently some magic flag I can set to auto-sync it but I can't find it
            HexMessages.getNetwork().send(PacketDistributor.PLAYER.with { target }, MsgAddMotionAck(motion))
        } else {
            target.deltaMovement = target.deltaMovement.add(motion)
        }
    }
}