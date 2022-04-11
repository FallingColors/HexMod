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
    @JvmStatic
    fun isManaItem(stack: ItemStack): Boolean {
        return extractMana(stack, simulate = true) > 0
    }

    /**
     * Extract [cost] mana from [stack]. If [cost] is less than zero, extract all mana instead.
     * This may mutate [stack] (and may consume it) unless [simulate] is set.
     *
     * If [drainFromBatteries] is false, this will not consider phials a valid source of mana.
     *
     * Return the amount of mana extracted. This may be over [cost] if mana is wasted.
     */
    @JvmStatic
    @JvmOverloads
    fun extractMana(stack: ItemStack, cost: Int = -1, drainFromBatteries: Boolean = true, simulate: Boolean = false): Int {
        val base = when (stack.item) {
            HexItems.AMETHYST_DUST.get() -> HexConfig.dustManaAmount.get()
            Items.AMETHYST_SHARD -> HexConfig.shardManaAmount.get()
            HexItems.CHARGED_AMETHYST.get() -> HexConfig.chargedCrystalManaAmount.get()

            is ItemManaBattery -> {
                if (drainFromBatteries) {
                    val battery = stack.item as ItemManaBattery
                    val tag = stack.orCreateTag
                    val manaThere = battery.getManaAmt(tag)
                    val manaToExtract = if (cost < 0) manaThere else min(cost, manaThere)
                    if (simulate)
                        return manaToExtract
                    return battery.withdrawMana(tag, manaToExtract)
                } else
                    return 0
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
                    is ItemManaBattery -> 4
                    else -> 0
                }
            intcode(aitem) - intcode(bitem)
        } else if (aitem is ItemManaBattery) {
            val atag = astack.orCreateTag
            val btag = bstack.orCreateTag
            aitem.getManaAmt(atag) - aitem.getManaAmt(btag)
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
