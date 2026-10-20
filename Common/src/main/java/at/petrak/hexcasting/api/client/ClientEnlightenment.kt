@file:JvmName("ClientEnlightenment")
package at.petrak.hexcasting.api.client

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.mixin.accessor.client.AccessorClientAdvancements
import net.minecraft.client.Minecraft

// based on https://github.com/DaFuqs/Revelationary/blob/1.20.1/src/main/java/de/dafuqs/revelationary/ClientAdvancements.java#L48
// the 1.21.1 version of that same code can be used as a reference when updating this to 1.21.1
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