package at.petrak.hex.common.casting

import at.petrak.hex.hexmath.HexPattern
import net.minecraft.world.phys.Vec3

class CastException(val reason: Reason, vararg val data: Any) : Exception() {
    enum class Reason {
        // Compilation
        /**
         * We couldn't match this pattern to an operator.
         *
         * `pattern: HexPattern`
         */
        INVALID_PATTERN,

        /**
         * Completely invalid type for spellcasting.
         * If you're seeing this error I messed up really bad
         *
         * `perpetrator: Any`
         */
        INVALID_TYPE,

        // Pre-execution
        /**
         * When executing an operator we expected a different type.
         *
         * `expected: Class<*>, got: Any`
         */
        OP_WRONG_TYPE,

        /**
         * We need at least this much on the stack to cast the spell but only got this much.
         *
         * `requiredArgc: Int, gotArgc: Int`
         */
        NOT_ENOUGH_ARGS,

        /**
         * There are too many close parentheses.
         *
         * `no args`
         */
        TOO_MANY_CLOSE_PARENS,

        // Execution
        /**
         * Tried to interact with a vector that was too far away
         *
         * `vec: Vec3`
         */
        TOO_FAR,

        /**
         * An operator needed a spellbook in the offhand.
         *
         * `no args`
         */
        REQUIRES_SPELLBOOK,

        /**
         * An operator needed a data holder in the offhand.
         *
         * `no args`
         */
        REQUIRES_DATA_HOLDER,

        /**
         * We went too deep!
         *
         * `maxDepth: Int, gotDepth: Int`
         */
        TOO_MANY_RECURSIVE_EVALS,
    }

    override val message: String
        get() = when (this.reason) {
            Reason.INVALID_PATTERN -> "could not match pattern to operator: ${this.data[0] as HexPattern}"
            Reason.INVALID_TYPE -> "cannot use ${this.data[0]} as a SpellDatum (type ${this.data[0].javaClass.typeName})"
            Reason.OP_WRONG_TYPE -> "operator expected ${(this.data[0] as Class<*>).typeName} but got ${this.data[1]} (type ${this.data[1].javaClass.typeName})"
            Reason.NOT_ENOUGH_ARGS -> "required at least ${this.data[0] as Int} args on the stack but only had ${this.data[1] as Int}"
            Reason.TOO_MANY_CLOSE_PARENS -> "too many close parentheses"
            Reason.TOO_FAR -> "tried to interact with something too far away at ${this.data[0] as Vec3}"
            Reason.REQUIRES_SPELLBOOK -> "required a spellbook in the other hand"
            Reason.REQUIRES_DATA_HOLDER -> "required a data holder in the other hand"
            Reason.TOO_MANY_RECURSIVE_EVALS -> "can only recursively call OpEval ${this.data[0] as Int} times but called it ${this.data[1] as Int} times"
        }
}