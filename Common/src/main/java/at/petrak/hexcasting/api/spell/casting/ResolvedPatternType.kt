package at.petrak.hexcasting.api.spell.casting

import java.util.*

enum class ResolvedPatternType(val color: Int, val fadeColor: Int, val success: Boolean) {
    UNRESOLVED(0x7f7f7f, 0xcccccc, false),
    EVALUATED(0x7385de, 0xfecbe6, true),
    ESCAPED(0xddcc73, 0xfffae5, true),
    ERRORED(0xde6262, 0xffc7a0, false),
    INVALID(0xb26b6b, 0xcca88e, false);

    companion object {
        @JvmStatic
        fun fromString(key: String): ResolvedPatternType {
            val lowercaseKey = key.lowercase(Locale.ROOT)
            return values().firstOrNull { it.name.lowercase(Locale.ROOT) == lowercaseKey } ?: UNRESOLVED
        }
    }
}
