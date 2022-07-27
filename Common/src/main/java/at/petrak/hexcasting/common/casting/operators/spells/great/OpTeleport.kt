package at.petrak.hexcasting.common.casting.operators.spells.great

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapImmuneEntity
import at.petrak.hexcasting.api.spell.mishaps.MishapLocationTooFarAway
import at.petrak.hexcasting.common.lib.HexEntityTags
import at.petrak.hexcasting.common.network.MsgBlinkAck
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

object OpTeleport : SpellOperator {
    override val argc = 2
    override val isGreat = true
    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val teleportee = args.getChecked<Entity>(0, argc)
        val delta = args.getChecked<Vec3>(1, argc)
        ctx.assertEntityInRange(teleportee)

        if (!teleportee.canChangeDimensions())
            throw MishapImmuneEntity(teleportee)

        val targetPos = teleportee.position().add(delta)
        ctx.assertVecInWorld(targetPos)
        if (!ctx.isVecInWorld(targetPos.subtract(0.0, 1.0, 0.0)))
            throw MishapLocationTooFarAway(targetPos, "too_close_to_out")

        val targetMiddlePos = teleportee.position().add(0.0, teleportee.eyeHeight / 2.0, 0.0)


        return Triple(
            Spell(teleportee, delta),
            10 * ManaConstants.CRYSTAL_UNIT,
            listOf(ParticleSpray.cloud(targetMiddlePos, 2.0), ParticleSpray.burst(targetMiddlePos.add(delta), 2.0))
        )
    }

    private data class Spell(val teleportee: Entity, val delta: Vec3) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val distance = delta.length()
            if (distance < 32768.0) {
                OpTeleport.teleportRespectSticky(teleportee, delta)
            }

            if (teleportee is ServerPlayer && teleportee == ctx.caster) {
                // Drop items conditionally, based on distance teleported.
                // MOST IMPORTANT: Never drop main hand item, since if it's a trinket, it will get duplicated later.

                val baseDropChance = distance / 10000.0

                // Armor and hotbar items have a further reduced chance to be dropped since it's particularly annoying
                // having to rearrange those. Also it makes sense for LORE REASONS probably, since the caster is more
                // aware of items they use often.
                for (armorItem in teleportee.inventory.armor) {
                    if (Math.random() < baseDropChance * 0.25) {
                        teleportee.drop(armorItem.copy(), true, false)
                        armorItem.shrink(armorItem.count)
                    }
                }

                for ((pos, invItem) in teleportee.inventory.items.withIndex()) {
                    if (invItem == teleportee.mainHandItem) continue
                    val dropChance = if (pos < 9) baseDropChance * 0.5 else baseDropChance // hotbar
                    if (Math.random() < dropChance) {
                        teleportee.drop(invItem.copy(), true, false)
                        invItem.shrink(invItem.count)
                    }
                }

                // we also don't drop the offhand just to be nice
            }
        }
    }

    fun teleportRespectSticky(teleportee: Entity, delta: Vec3) {
        var stickyTeleport = false
        // roll our own for loop
        // this really would be more readable as a c-style for loop, waugh
        var cursor = teleportee.vehicle
        var base: Entity? = null
        while (cursor != null) {
            if (cursor.type.`is`(HexEntityTags.STICKY_TELEPORTERS))
                stickyTeleport = true
            base = cursor
            cursor = cursor.vehicle
        }

        if (stickyTeleport) {
            // this handles teleporting the passengers
            val target = base!!.position().add(delta)
            base.teleportTo(target.x, target.y, target.z)
        } else {
            // Break it into two stacks
            teleportee.stopRiding()
            teleportee.firstPassenger?.stopRiding()
            teleportee.setPos(teleportee.position().add(delta))
        }

        teleportee.setPos(teleportee.position().add(delta))
        if (teleportee is ServerPlayer) {
            teleportee.connection.resetPosition()
            IXplatAbstractions.INSTANCE.sendPacketToPlayer(teleportee, MsgBlinkAck(delta))
        }
    }
}
