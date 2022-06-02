@file:JvmName("AccessorWrappers")
package at.petrak.hexcasting.ktxt

import at.petrak.hexcasting.mixin.accessor.AccessorLivingEntity
import at.petrak.hexcasting.mixin.accessor.AccessorUseOnContext
import at.petrak.hexcasting.mixin.accessor.AccessorVillager
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult

var LivingEntity.lastHurt: Float
    get() = (this as AccessorLivingEntity).`hex$getLastHurt`()
    set(value) = (this as AccessorLivingEntity).`hex$setLastHurt`(value)

fun Villager.tellWitnessesThatIWasMurdered(murderer: Entity) = (this as AccessorVillager).`hex$tellWitnessesThatIWasMurdered`(murderer)

@Suppress("FunctionName")
fun UseOnContext(level: Level, player: Player?, hand: InteractionHand, stack: ItemStack, hitResult: BlockHitResult): UseOnContext =
    AccessorUseOnContext.`hex$new`(level, player, hand, stack, hitResult)
