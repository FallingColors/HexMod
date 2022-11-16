@file:JvmName("MediaHelper")

package at.petrak.hexcasting.api.utils

import at.petrak.hexcasting.api.addldata.ADMediaHolder
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemStack
import kotlin.math.roundToInt

fun isMediaItem(stack: ItemStack): Boolean {
    val mediaHolder = IXplatAbstractions.INSTANCE.findMediaHolder(stack) ?: return false
    if (!mediaHolder.canProvide())
        return false
    return mediaHolder.withdrawMedia(-1, true) > 0
}

/**
 * Extract [cost] media from [stack]. If [cost] is less than zero, extract all media instead.
 * This may mutate [stack] (and may consume it) unless [simulate] is set.
 *
 * If [drainForBatteries] is false, this will only consider forms of media that can be used to make new batteries.
 *
 * Return the amount of media extracted. This may be over [cost] if media is wasted.
 */
@JvmOverloads
fun extractMedia(
    stack: ItemStack,
    cost: Int = -1,
    drainForBatteries: Boolean = false,
    simulate: Boolean = false
): Int {
    val mediaHolder = IXplatAbstractions.INSTANCE.findMediaHolder(stack) ?: return 0

    return extractMedia(mediaHolder, cost, drainForBatteries, simulate)
}

/**
 * Extract [cost] media from [holder]. If [cost] is less than zero, extract all media instead.
 * This may mutate the stack underlying [holder] (and may consume it) unless [simulate] is set.
 *
 * If [drainForBatteries] is false, this will only consider forms of media that can be used to make new batteries.
 *
 * Return the amount of media extracted. This may be over [cost] if media is wasted.
 */
fun extractMedia(
    holder: ADMediaHolder,
    cost: Int = -1,
    drainForBatteries: Boolean = false,
    simulate: Boolean = false
): Int {
    if (drainForBatteries && !holder.canConstructBattery())
        return 0

    return holder.withdrawMedia(cost, simulate)
}

/**
 * Sorted from least important to most important
 */
fun compareMediaItem(aMedia: ADMediaHolder, bMedia: ADMediaHolder): Int {
    val priority = aMedia.consumptionPriority - bMedia.consumptionPriority
    if (priority != 0)
        return priority

    return aMedia.withdrawMedia(-1, true) - bMedia.withdrawMedia(-1, true)
}

fun mediaBarColor(media: Int, maxMedia: Int): Int {
    val amt = if (maxMedia == 0) {
        0f
    } else {
        media.toFloat() / maxMedia.toFloat()
    }

    val r = Mth.lerp(amt, 84f, 254f)
    val g = Mth.lerp(amt, 57f, 203f)
    val b = Mth.lerp(amt, 138f, 230f)
    return Mth.color(r / 255f, g / 255f, b / 255f)
}

fun mediaBarWidth(media: Int, maxMedia: Int): Int {
    val amt = if (maxMedia == 0) {
        0f
    } else {
        media.toFloat() / maxMedia.toFloat()
    }
    return (13f * amt).roundToInt()
}
