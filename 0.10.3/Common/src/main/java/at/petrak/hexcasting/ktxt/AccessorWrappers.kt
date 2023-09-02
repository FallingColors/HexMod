@file:JvmName("AccessorWrappers")
package at.petrak.hexcasting.ktxt

import at.petrak.hexcasting.mixin.accessor.AccessorEntity
import at.petrak.hexcasting.mixin.accessor.AccessorLivingEntity
import at.petrak.hexcasting.mixin.accessor.AccessorUseOnContext
import at.petrak.hexcasting.mixin.accessor.AccessorVillager
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.InteractionHand
import net.minecraft.world.damagesource.DamageSource
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

fun LivingEntity.playHurtSound(source: DamageSource) = (this as AccessorLivingEntity).`hex$playHurtSound`(source)
fun LivingEntity.checkTotemDeathProtection(source: DamageSource) = (this as AccessorLivingEntity).`hex$checkTotemDeathProtection`(source)
val LivingEntity.deathSoundAccessor: SoundEvent? get() = (this as AccessorLivingEntity).`hex$getDeathSound`()
val LivingEntity.soundVolumeAccessor get() = (this as AccessorLivingEntity).`hex$getSoundVolume`()

fun LivingEntity.setHurtWithStamp(source: DamageSource, stamp: Long) = (this as AccessorLivingEntity).apply {
    `hex$setLastDamageSource`(source)
    `hex$setLastDamageStamp`(stamp)
}

fun Entity.markHurt() = (this as AccessorEntity).`hex$markHurt`()

fun Villager.tellWitnessesThatIWasMurdered(murderer: Entity) = (this as AccessorVillager).`hex$tellWitnessesThatIWasMurdered`(murderer)

@Suppress("FunctionName")
fun UseOnContext(level: Level, player: Player?, hand: InteractionHand, stack: ItemStack, hitResult: BlockHitResult): UseOnContext =
    AccessorUseOnContext.`hex$new`(level, player, hand, stack, hitResult)
