package at.petrak.hexcasting.common.casting

import at.petrak.hexcasting.HexConfig
import at.petrak.hexcasting.common.items.HexItems
import at.petrak.hexcasting.common.items.magic.ItemManaBattery
import net.minecraft.util.Mth
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

object ManaHelper {
    fun isManaItem(stack: ItemStack): Boolean {
        return stack.`is`(HexItems.AMETHYST_DUST.get())
                || stack.`is`(Items.AMETHYST_SHARD)
                || stack.`is`(HexItems.CHARGED_AMETHYST.get())
                || stack.item is ItemManaBattery
    }

    /**
     * Try to extract the given amount of mana from this item.
     * This may mutate the itemstack.
     *
     * Return the actual amount of mana extracted, or null if this cannot have mana extracted.
     */
    fun extractMana(stack: ItemStack, cost: Int): Int? {
        val base = when (stack.item) {
            HexItems.AMETHYST_DUST.get() -> HexConfig.dustManaAmount.get()
            Items.AMETHYST_SHARD -> HexConfig.shardManaAmount.get()
            HexItems.CHARGED_AMETHYST.get() -> HexConfig.chargedCrystalManaAmount.get()
            HexItems.BATTERY.get() -> {
                val battery = stack.item as ItemManaBattery
                return battery.withdrawMana(stack.orCreateTag, cost)
            }
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
     * Return the amount of mana extracted.
     */
    fun extractAllMana(stack: ItemStack): Int {
        val base = when (stack.item) {
            HexItems.AMETHYST_DUST.get() -> HexConfig.dustManaAmount.get()
            Items.AMETHYST_SHARD -> HexConfig.shardManaAmount.get()
            HexItems.CHARGED_AMETHYST.get() -> HexConfig.chargedCrystalManaAmount.get()

            HexItems.BATTERY.get() -> {
                val battery = stack.item as ItemManaBattery
                val tag = stack.orCreateTag
                val manaThere = battery.getManaAmt(tag)
                return battery.withdrawMana(tag, manaThere)
            }
            else -> return 0
        }
        val count = stack.count
        stack.shrink(count)
        return base * count
    }

    /**
     * Sorted from least important to most important
     */
    fun compare(astack: ItemStack, bstack: ItemStack): Int {
        val aitem = astack.item
        val bitem = bstack.item

        return if (aitem != bitem) {
            fun intcode(item: Item): Int =
                when (item) {
                    HexItems.CHARGED_AMETHYST.get() -> 1
                    Items.AMETHYST_SHARD -> 2
                    HexItems.AMETHYST_DUST.get() -> 3
                    HexItems.BATTERY.get() -> 4
                    else -> 0
                }
            intcode(aitem) - intcode(bitem)
        } else if (aitem == HexItems.BATTERY.get()) {
            val atag = astack.orCreateTag
            val btag = bstack.orCreateTag
            val battery = aitem as ItemManaBattery
            battery.getManaAmt(atag) - battery.getManaAmt(btag)
        } else {
            astack.count - bstack.count
        }
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
