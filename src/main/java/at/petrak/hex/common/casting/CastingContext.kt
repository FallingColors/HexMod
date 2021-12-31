package at.petrak.hex.common.casting

import at.petrak.hex.HexMod
import at.petrak.hex.HexUtils
import at.petrak.hex.api.Operator
import at.petrak.hex.common.items.ItemDataHolder
import at.petrak.hex.common.items.ItemSpellbook
import at.petrak.hex.common.items.ItemWand
import at.petrak.hex.common.lib.LibDamageSources
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import java.util.function.Predicate
import kotlin.math.max
import kotlin.math.min

/**
 * Info about the moment the spell started being cast.
 */
data class CastingContext(
    val caster: ServerPlayer,
    val wandHand: InteractionHand,
) {
    private var depth: Int = 0
    val world: ServerLevel get() = caster.getLevel()
    val otherHand: InteractionHand get() = HexUtils.OtherHand(this.wandHand)

    fun getSpellbook(): ItemStack {
        val handItem =
            caster.getItemInHand(this.otherHand)
        return if (handItem.item is ItemSpellbook) {
            handItem
        } else {
            throw CastException(CastException.Reason.REQUIRES_SPELLBOOK)
        }
    }

    fun getDataHolder(): ItemStack {
        val handItem =
            caster.getItemInHand(this.otherHand)
        return if (handItem.item is ItemDataHolder) {
            handItem
        } else {
            throw CastException(CastException.Reason.REQUIRES_DATA_HOLDER)
        }
    }

    /**
     * Throws if we get too deep
     */
    fun withIncDepth(): CastingContext {
        val next = this.copy()
        next.depth++

        val maxAllowedDepth = HexMod.CONFIG.maxRecurseDepth.get()
        if (next.depth > maxAllowedDepth) {
            throw CastException(CastException.Reason.TOO_MANY_RECURSIVE_EVALS, maxAllowedDepth, next.depth)
        } else {
            return next
        }
    }

    /**
     * Might cast from hitpoints.
     * Returns the mana cost still remaining after we deplete everything. It will be <= 0 if we could pay for it. */
    fun withdrawMana(manaCost: Int, allowOvercast: Boolean): Int {
        var costLeft = manaCost

        val held = caster.getItemInHand(this.wandHand)
        if (held.item is ItemWand) {
            val tag = held.orCreateTag
            val manaHere = tag.getInt(ItemWand.TAG_MANA)
            val manaLeft = manaHere - costLeft
            tag.putInt(ItemWand.TAG_MANA, max(0, manaLeft))
            costLeft = max(0, costLeft - manaHere)
        }
        if (allowOvercast && costLeft > 0) {
            // Cast from HP!
            val healthToMana = HexMod.CONFIG.healthToManaRate.get()
            val healthtoRemove = healthToMana * costLeft.toDouble()
            val manaAbleToCastFromHP =
                if (caster.isInvulnerable) Double.POSITIVE_INFINITY else caster.health / healthToMana
            caster.hurt(LibDamageSources.OVERCAST, healthtoRemove.toFloat())
            costLeft = (costLeft.toDouble() - manaAbleToCastFromHP).toInt()
        }
        return costLeft
    }

    fun assertVecInRange(vec: Vec3) {
        if (vec.distanceToSqr(this.caster.position()) > Operator.MAX_DISTANCE * Operator.MAX_DISTANCE)
            throw CastException(CastException.Reason.TOO_FAR, vec)
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
        val anchorSlot = when (this.wandHand) {
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
     * Remove the given gound of the specified item from somewhere in the inventory, favoring slots not in the hotbar.
     * Return whether the withdrawal was successful.
     */
    // https://github.com/VazkiiMods/Psi/blob/master/src/main/java/vazkii/psi/common/spell/trick/block/PieceTrickPlaceBlock.java#L143
    fun withdrawItem(item: Item, count: Int, actuallyRemove: Boolean): Boolean {
        if (this.caster.isCreative) return true

        val inv = this.caster.inventory
        // TODO: withdraw from ender chest given a specific ender charm?
        val stacksToExamine = inv.items.asReversed()

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
                if (remaining == 0) {
                    return true
                }
            }
        }
        throw RuntimeException("unreachable")
    }
}
