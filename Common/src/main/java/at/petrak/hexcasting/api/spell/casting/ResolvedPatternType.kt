package at.petrak.hexcasting.api.spell.casting

enum class ResolvedPatternType(val color: Int, val fadeColor: Int, val success: Boolean) {
    UNKNOWN(0x7f7f7f, 0xcccccc, false),
    OK(0x7385de, 0xfecbe6, true),
    PATTERN(0xdddd73, 0xffffe5, true),
    ERROR(0xde6262, 0xffc7a0, false),
    INVALID(0xddbcbc, 0xfff0e5, false)
}
