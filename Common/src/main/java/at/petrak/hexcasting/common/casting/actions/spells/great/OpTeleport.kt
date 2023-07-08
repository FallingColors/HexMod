package at.petrak.hexcasting.common.casting.actions.spells.great

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadLocation
import at.petrak.hexcasting.api.casting.mishaps.MishapImmuneEntity
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.mod.HexTags
import at.petrak.hexcasting.common.msgs.MsgBlinkS2C
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.TicketType
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.phys.Vec3

// TODO while we're making breaking changes I *really* want to have the vector in the entity's local space
// WRT its look vector
object OpTeleport : SpellAction {
    override val argc = 2
    override fun execute(
            args: List<Iota>,
            env: CastingEnvironment
    ): SpellAction.Result {

        val teleportee = args.getEntity(0, argc)
        val delta = args.getVec3(1, argc)
        env.assertEntityInRange(teleportee)

        if (!teleportee.canChangeDimensions() || teleportee.type.`is`(HexTags.Entities.CANNOT_TELEPORT))
            throw MishapImmuneEntity(teleportee)

        val targetPos = teleportee.position().add(delta)
        if (!HexConfig.server().canTeleportInThisDimension(env.world.dimension()))
            throw MishapBadLocation(targetPos, "bad_dimension")

        env.assertVecInWorld(targetPos)
        if (!env.isVecInWorld(targetPos.subtract(0.0, 1.0, 0.0)))
            throw MishapBadLocation(targetPos, "too_close_to_out")

        val targetMiddlePos = teleportee.position().add(0.0, teleportee.eyeHeight / 2.0, 0.0)

        return SpellAction.Result(
            Spell(teleportee, delta),
            10 * MediaConstants.CRYSTAL_UNIT,
            listOf(ParticleSpray.cloud(targetMiddlePos, 2.0), ParticleSpray.burst(targetMiddlePos.add(delta), 2.0))
        )
    }

    private data class Spell(val teleportee: Entity, val delta: Vec3) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val distance = delta.length()

            teleportRespectSticky(teleportee, delta, env.world)

            if (teleportee is ServerPlayer && teleportee == env.caster) {
                // Drop items conditionally, based on distance teleported.
                // MOST IMPORTANT: Never drop main hand item, since if it's a trinket, it will get duplicated later.

                val baseDropChance = distance / 10000.0

                // Armor and hotbar items have a further reduced chance to be dropped since it's particularly annoying
                // having to rearrange those. Also it makes sense for LORE REASONS probably, since the caster is more
                // aware of items they use often.
                for (armorItem in teleportee.inventory.armor) {
                    if (EnchantmentHelper.hasBindingCurse(armorItem))
                        continue

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

    fun teleportRespectSticky(teleportee: Entity, delta: Vec3, world: ServerLevel) {
        if (!HexConfig.server().canTeleportInThisDimension(world.dimension())) {
            return
        }

        val playersToUpdate = mutableListOf<ServerPlayer>()
        val target = teleportee.position().add(delta)

        val cannotTeleport = teleportee.passengers.any { it.type.`is`(HexTags.Entities.CANNOT_TELEPORT) }
        if (cannotTeleport)
            return

        // A "sticky" entity teleports itself and its riders
        val sticky = teleportee.type.`is`(HexTags.Entities.STICKY_TELEPORTERS)

        // TODO: this probably does funky things with stacks of passengers. I doubt this will come up in practice
        // though
        if (sticky) {
            teleportee.stopRiding()
            teleportee.indirectPassengers.filterIsInstance<ServerPlayer>().forEach(playersToUpdate::add)
            // this handles teleporting the passengers
            teleportee.teleportTo(target.x, target.y, target.z)
        } else {
            // Snap everyone off the stacks
            teleportee.stopRiding()
            teleportee.passengers.forEach(Entity::stopRiding)
            if (teleportee is ServerPlayer) {
                playersToUpdate.add(teleportee)
            } else {
                teleportee.setPos(teleportee.position().add(delta))
            }
        }

        for (player in playersToUpdate) {
            // See TeleportCommand
            val chunkPos = ChunkPos(BlockPos.containing(delta))
            // the `1` is apparently for "distance." i'm not sure what it does but this is what
            // /tp does
            world.chunkSource.addRegionTicket(TicketType.POST_TELEPORT, chunkPos, 1, player.id)
            player.connection.resetPosition()
            player.setPos(target)
            IXplatAbstractions.INSTANCE.sendPacketToPlayer(player, MsgBlinkS2C(delta))
        }
    }
}
