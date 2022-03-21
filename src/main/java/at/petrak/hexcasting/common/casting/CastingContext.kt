package at.petrak.hexcasting.common.casting

import at.petrak.hexcasting.HexConfig
import at.petrak.hexcasting.HexUtils
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.common.items.ItemDataHolder
import at.petrak.hexcasting.common.items.ItemSpellbook
import at.petrak.hexcasting.common.lib.HexCapabilities
import at.petrak.hexcasting.common.lib.RegisterHelper.prefix
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import java.util.function.Predicate
import kotlin.math.min

/**
 * Info about the moment the spell started being cast.
 */
data class CastingContext(
    val caster: ServerPlayer,
    val castingHand: InteractionHand,
) {
    private var depth: Int = 0

    val world: ServerLevel get() = caster.getLevel()
    val otherHand: InteractionHand get() = HexUtils.OtherHand(this.castingHand)
    val position: Vec3 get() = caster.position()


    fun getSpellbook(): ItemStack {
        val handItem =
            caster.getItemInHand(this.otherHand)
        return if (handItem.item is ItemSpellbook) {
            handItem
        } else {
            throw CastException(CastException.Reason.BAD_OFFHAND_ITEM, ItemSpellbook::class.java, handItem)
        }
    }

    fun getDataHolder(): ItemStack {
        val handItem =
            caster.getItemInHand(this.otherHand)
        return if (handItem.item is ItemDataHolder) {
            handItem
        } else {
            throw CastException(CastException.Reason.BAD_OFFHAND_ITEM, ItemDataHolder::class.java, handItem)
        }
    }

    /**
     * Throws if we get too deep
     */
    fun incDepth() {
        this.depth++
        val maxAllowedDepth = HexConfig.maxRecurseDepth.get()
        if (this.depth > maxAllowedDepth) {
            throw CastException(CastException.Reason.TOO_MANY_RECURSIVE_EVALS, maxAllowedDepth, this.depth)
        }
    }

    /**
     * Check to make sure a vec is in range
     */
    fun assertVecInRange(vec: Vec3) {
        if (vec.distanceToSqr(this.caster.position()) < Operator.MAX_DISTANCE * Operator.MAX_DISTANCE)
            return

        val maybeSentinel = this.caster.getCapability(HexCapabilities.SENTINEL).resolve()
        if (maybeSentinel.isPresent) {
            val sentinel = maybeSentinel.get()
            if (sentinel.hasSentinel
                && sentinel.extendsRange
                && vec.distanceToSqr(sentinel.position) < Operator.MAX_DISTANCE_FROM_SENTINEL * Operator.MAX_DISTANCE_FROM_SENTINEL
            )
                return
        }

        throw CastException(CastException.Reason.TOO_FAR, vec)
    }

    /**
     * Return the slot from which to take blocks and itemsn.
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
        val stacksToExamine = inv.items.asReversed().toMutableList()
        stacksToExamine.addAll(inv.offhand)

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
