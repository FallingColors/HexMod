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
     * Extract [cost] mana from [stack]. If [cost] is less than zero, extract all mana instead.
     * This may mutate [stack] (and may consume it) unless [simulate] is set.
     *
     * Return the amount of mana extracted. This may be over [cost] if mana is wasted.
     */
    fun extractMana(stack: ItemStack, cost: Int = -1, simulate: Boolean = false): Int {
        val base = when (stack.item) {
            HexItems.AMETHYST_DUST.get() -> HexConfig.dustManaAmount.get()
            Items.AMETHYST_SHARD -> HexConfig.shardManaAmount.get()
            HexItems.CHARGED_AMETHYST.get() -> HexConfig.chargedCrystalManaAmount.get()

            HexItems.BATTERY.get() -> {
                val battery = stack.item as ItemManaBattery
                val tag = stack.orCreateTag
                val manaThere = battery.getManaAmt(tag)
                val manaToExtract = if (cost < 0) manaThere else min(cost, manaThere)
                if (simulate)
                    return manaToExtract
                return battery.withdrawMana(tag, manaToExtract)
            }
            else -> return 0
        }
        val count = stack.count
        val countToExtract = if (cost < 0) count else min(count, ceil(cost.toDouble() / base).toInt())
        if (!simulate)
            stack.shrink(countToExtract)
        return base * countToExtract
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
