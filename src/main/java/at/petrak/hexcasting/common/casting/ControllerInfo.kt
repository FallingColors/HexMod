package at.petrak.hexcasting.common.casting

/**
 * Information for the sake of the GUI.
 */
data class ControllerInfo(val status: Status) {
    enum class Status {
        NONE,
        SPELL_CAST,
        SPELL_CAST_AND_DONE,
        PREV_PATTERN_INVALID,
    }

    fun shouldQuit(): Boolean = this.status == Status.SPELL_CAST_AND_DONE
    fun wasSpellCast(): Boolean = this.status == Status.SPELL_CAST || this.status == Status.SPELL_CAST_AND_DONE
}