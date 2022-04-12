package at.petrak.hexcasting.api.spell.casting

import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.player.HexPlayerDataHelper
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.mishaps.MishapEntityTooFarAway
import at.petrak.hexcasting.api.spell.mishaps.MishapEvalTooDeep
import at.petrak.hexcasting.api.spell.mishaps.MishapLocationTooFarAway
import at.petrak.hexcasting.api.utils.HexUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import java.util.function.Predicate
import kotlin.math.min

/**
 * Transient info about the moment the spell started being cast.
 */
data class CastingContext(
    val caster: ServerPlayer,
    val castingHand: InteractionHand,
    val spellCircle: SpellCircleContext? = null
) {
    constructor(caster: ServerPlayer, castingHand: InteractionHand) : this(caster, castingHand, null)

    private var depth: Int = 0

    val world: ServerLevel get() = caster.getLevel()
    val otherHand: InteractionHand get() = HexUtils.OtherHand(this.castingHand)
    val position: Vec3 get() = caster.position()

    private val entitiesGivenMotion = mutableSetOf<Entity>()

    inline fun getHeldItemToOperateOn(acceptItemIf: (ItemStack) -> Boolean): Pair<ItemStack, InteractionHand> {
        if (this.spellCircle == null) {
            return caster.getItemInHand(otherHand) to otherHand
        }

        val handItem = caster.getItemInHand(castingHand)
        if (!acceptItemIf(handItem))
            return caster.getItemInHand(otherHand) to otherHand
        return handItem to castingHand
    }

    /**
     * Throws if we get too deep
     */
    fun incDepth() {
        this.depth++
        val maxAllowedDepth = HexConfig.maxRecurseDepth.get()
        if (this.depth > maxAllowedDepth) {
            throw MishapEvalTooDeep()
        }
    }

    /**
     * Check to make sure a vec is in range
     */
    fun assertVecInRange(vec: Vec3) {
        if (!isVecInRange(vec)) throw MishapLocationTooFarAway(vec)
    }

    /**
     * Check to make sure an entity is in range
     */
    fun assertEntityInRange(entity: Entity) {
        if (!isEntityInRange(entity)) throw MishapEntityTooFarAway(entity)
    }

    fun hasBeenGivenMotion(target: Entity): Boolean {
        return entitiesGivenMotion.contains(target)
    }

    fun isVecInRange(vec: Vec3): Boolean {
        val sentinel = HexPlayerDataHelper.getSentinel(caster)
        if (sentinel.hasSentinel
            && sentinel.extendsRange
            && world.dimension() == sentinel.dimension
            && vec.distanceToSqr(sentinel.position) < Operator.MAX_DISTANCE_FROM_SENTINEL * Operator.MAX_DISTANCE_FROM_SENTINEL
        )
            return true


        if (this.spellCircle != null) {
            // we use the eye position cause thats where the caster gets their "position" from
            if (this.spellCircle.activatorAlwaysInRange && vec.distanceToSqr(this.caster.eyePosition) < 2 * 2)
                return true
            return this.spellCircle.aabb.contains(vec)
        }

        if (vec.distanceToSqr(this.caster.position()) < Operator.MAX_DISTANCE * Operator.MAX_DISTANCE)
            return true

        return false
    }

    fun isEntityInRange(entity: Entity): Boolean {
        if (this.spellCircle != null && this.spellCircle.activatorAlwaysInRange && entity == this.caster) {
            return true
        }
        return isVecInRange(entity.position())
    }

    /**
     * Return the slot from which to take blocks and items.
     */
    // https://wiki.vg/Inventory is WRONG
    // slots 0-8 are the hotbar
    // for what purpose i cannot imagine
    // http://redditpublic.com/images/b/b2/Items_slot_number.png looks right
    // and offhand is 150 Inventory.java:464
    fun getOperativeSlot(stackOK: Predicate<ItemStack>): Int? {
        val otherHandStack = this.caster.getItemInHand(this.otherHand)
        if (stackOK.test(otherHandStack)) {
            return when (this.otherHand) {
                InteractionHand.MAIN_HAND -> this.caster.inventory.selected
                InteractionHand.OFF_HAND -> 150
            }
        }
        val anchorSlot = when (this.castingHand) {
            // slot to the right of the wand
            InteractionHand.MAIN_HAND -> (this.caster.inventory.selected + 1) % 9
            // first hotbar slot
            InteractionHand.OFF_HAND -> 0
        }
        for (delta in 0 until 9) {
            val slot = (anchorSlot + delta) % 9
            val stack = this.caster.inventory.getItem(slot)
            if (stackOK.test(stack)) {
                return slot
            }
        }
        return null
    }

    /**
     * Remove the given count of the specified item from somewhere in the inventory, favoring slots not in the hotbar.
     * Return whether the withdrawal was successful.
     */
    // https://github.com/VazkiiMods/Psi/blob/master/src/main/java/vazkii/psi/common/spell/trick/block/PieceTrickPlaceBlock.java#L143
    fun withdrawItem(item: Item, count: Int, actuallyRemove: Boolean): Boolean {
        if (this.caster.isCreative) return true

        val inv = this.caster.inventory
        // TODO: withdraw from ender chest given a specific ender charm?
        val stacksToExamine = inv.items.toMutableList().apply { removeAt(inv.selected) }.asReversed().toMutableList()
        stacksToExamine.addAll(inv.offhand)
        stacksToExamine.add(inv.getSelected())

        fun matches(stack: ItemStack): Boolean =
            !stack.isEmpty && stack.`is`(item)

        val presentCount = stacksToExamine.fold(0) { acc, stack ->
            acc + if (matches(stack)) stack.count else 0
        }
        if (presentCount < count) return false

        // now that we know we have enough items, if we don't need to remove anything we're through.
        if (!actuallyRemove) return true

        var remaining = count
        for (stack in stacksToExamine) {
            if (matches(stack)) {
                val toWithdraw = min(stack.count, remaining)
                stack.shrink(toWithdraw)

                remaining -= toWithdraw
                if (remaining <= 0) {
                    return true
                }
            }
        }
        throw RuntimeException("unreachable")
    }

    fun markEntityAsMotionAdded(entity: Entity) {
        this.entitiesGivenMotion.add(entity)
    }

    private fun prefix(path: String): ResourceLocation {
        return ResourceLocation("hexcasting", path)
    }

    val canOvercast: Boolean
        get() {
            val adv = this.world.server.advancements.getAdvancement(prefix("y_u_no_cast_angy"))
            val advs = this.caster.advancements
            return advs.getOrStartProgress(adv!!).isDone
        }

    val isCasterEnlightened: Boolean
        get() {
            val adv = this.world.server.advancements.getAdvancement(prefix("enlightenment"))
            val advs = this.caster.advancements
            return advs.getOrStartProgress(adv!!).isDone
        }
}
