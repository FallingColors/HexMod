package at.petrak.hex.common.casting

import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

/**
 * Info about the moment the spell started being cast.
 */
@JvmRecord
data class CastingContext(
    val caster: ServerPlayer,
) {
    val world: ServerLevel get() = caster.getLevel()
}
