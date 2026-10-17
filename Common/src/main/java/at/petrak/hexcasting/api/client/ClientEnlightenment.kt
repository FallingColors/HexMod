@file:JvmName("ClientEnlightenment")
package at.petrak.hexcasting.api.client

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.mixin.accessor.client.AccessorClientAdvancements
import net.minecraft.client.Minecraft

fun isEnlightened(): Boolean {
    val conn = Minecraft.getInstance().connection
    if (conn != null) {
        val enlightenAdv = conn.advancements.advancements.get(HexAPI.modLoc("enlightenment"))
        if (enlightenAdv != null) {
            val progressMap = (conn.advancements as AccessorClientAdvancements).`hex$getProgress`()
            val enlightenProgress = progressMap[enlightenAdv]
            return enlightenProgress != null && enlightenProgress.isDone
        }
    }
    return false
}