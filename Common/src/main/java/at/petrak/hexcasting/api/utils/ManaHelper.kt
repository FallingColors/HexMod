package at.petrak.hexcasting.api.utils

import at.petrak.hexcasting.api.cap.HexCapabilities
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemStack
import kotlin.math.roundToInt

object ManaHelper {
    @JvmStatic
    fun isManaItem(stack: ItemStack): Boolean {
        return stack.getCapability(HexCapabilities.MANA).map { it.canProvide() }.orElse(false) && extractMana(stack, simulate = true) > 0
    }

    /**
     * Extract [cost] mana from [stack]. If [cost] is less than zero, extract all mana instead.
     * This may mutate [stack] (and may consume it) unless [simulate] is set.
     *
     * If [drainForBatteries] is false, this will only consider forms of mana that can be used to make new batteries.
     *
     * Return the amount of mana extracted. This may be over [cost] if mana is wasted.
     */
    @JvmStatic
    @JvmOverloads
    fun extractMana(stack: ItemStack, cost: Int = -1, drainForBatteries: Boolean = false, simulate: Boolean = false): Int {
        val manaCapability = stack.getCapability(HexCapabilities.MANA).resolve()

        if (!manaCapability.isPresent)
            return 0

        val manaReservoir = manaCapability.get()

        if (drainForBatteries && !manaReservoir.canConstructBattery())
            return 0

        return manaReservoir.withdrawMana(cost, simulate)
    }

    /**
     * Sorted from least important to most important
     */
    fun compare(astack: ItemStack, bstack: ItemStack): Int {
        val aMana = astack.getCapability(HexCapabilities.MANA).resolve()
        val bMana = bstack.getCapability(HexCapabilities.MANA).resolve()

        return if (astack.item != bstack.item) {
            aMana.map { it.consumptionPriority }.orElse(0) - bMana.map { it.consumptionPriority }.orElse(0)
        } else if (aMana.isPresent && bMana.isPresent) {
            aMana.get().mana - bMana.get().mana
        } else {
            astack.count - bstack.count
        }
    }

    @JvmStatic
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

    @JvmStatic
    fun barWidth(mana: Int, maxMana: Int): Int {
        val amt = if (maxMana == 0) {
            0f
        } else {
            mana.toFloat() / maxMana.toFloat()
        }
        return (13f * amt).roundToInt()
    }
}
