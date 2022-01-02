package at.petrak.hex.common.casting

import at.petrak.hex.HexMod
import at.petrak.hex.common.items.HexItems
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

object ManaHelper {
    /**
     * Try to extract the given amount of mana from this item.
     * This may mutate the itemstack.
     *
     * Return the actual amount of mana extracted, or null if this cannot have mana extracted.
     */
    fun extractMana(stack: ItemStack, cost: Int): Int? {
        val base = when (stack.item) {
            HexItems.AMETHYST_DUST.get() -> HexMod.CONFIG.dustManaAmount.get()
            Items.AMETHYST_SHARD -> HexMod.CONFIG.shardManaAmount.get()
            HexItems.CHARGED_AMETHYST.get() -> HexMod.CONFIG.chargedCrystalManaAmount.get()
            else -> return null
        }
        val itemsReqd = ceil(cost.toFloat() / base.toFloat()).toInt()
        val actualItemsConsumed = min(stack.count, itemsReqd)
        stack.shrink(actualItemsConsumed)
        return base * actualItemsConsumed
    }

    /**
     * Extract the entirety of the mana out of this.
     * This may mutate the itemstack (and will probably consume it).
     *
     * Return the amount of mana extracted, or null if this cannot have mana extracted.
     */
    fun extractAllMana(stack: ItemStack): Int? {
        val base = when (stack.item) {
            HexItems.AMETHYST_DUST.get() -> HexMod.CONFIG.dustManaAmount.get()
            Items.AMETHYST_SHARD -> HexMod.CONFIG.shardManaAmount.get()
            HexItems.CHARGED_AMETHYST.get() -> HexMod.CONFIG.chargedCrystalManaAmount.get()
            else -> return null
        }
        val count = stack.count
        stack.shrink(count)
        return base * count
    }

    /**
     * Return the "priority" this should have mana extracted with.
     * Higher numbers mean it should be extracted more eagerly.
     * Null means it isn't a mana item.
     */
    fun priority(stack: ItemStack): Int? {
        val base = 100 * when (stack.item) {
            HexItems.CHARGED_AMETHYST.get() -> 1
            Items.AMETHYST_SHARD -> 2
            HexItems.AMETHYST_DUST.get() -> 3
            else -> return null
        }
        return base + stack.count
    }

    fun barColor(mana: Int, maxMana: Int): Int {
        val amt = if (maxMana == 0) {
            0f
        } else {
            mana.toFloat() / maxMana.toFloat()
        }

        val r = Mth.lerp(amt, 84f, 254f)
        val g = Mth.lerp(amt, 57f, 203f)
        val b = Mth.lerp(amt, 138f, 230f)
        return Mth.color(r / 255f, g / 255f, b / 255f)
    }

    fun barWidth(mana: Int, maxMana: Int): Int {
        val amt = if (maxMana == 0) {
            0f
        } else {
            mana.toFloat() / maxMana.toFloat()
        }
        return (13f * amt).roundToInt()
    }
}