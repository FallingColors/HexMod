package at.petrak.hexcasting.api.spell.casting.sideeffects

/**
 * Package for all the side effects that happen during one cast.
 *
 * This lives outside of nested evaluations so we don't get giant sound spam
 */
class SideEffectsTracker private constructor(private val ops: MutableList<OperatorSideEffect>, private var sound: EvalSound) {
    public constructor() : this(mutableListOf(), EvalSound.NONE)
}