package at.petrak.hex.common.casting

import at.petrak.hex.hexmath.HexPattern
import net.minecraft.world.phys.Vec3

class CastException(val reason: at.petrak.hex.common.casting.CastException.Reason, vararg val data: Any) : Exception() {
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

        // Execution
        /**
         * Tried to interact with a vector that was too far away
         *
         * `vec: Vec3`
         */
        TOO_FAR,
    }

    override val message: String
        get() = when (this.reason) {
            at.petrak.hex.common.casting.CastException.Reason.INVALID_PATTERN -> "could not match pattern to operator: ${this.data[0] as HexPattern}"
            at.petrak.hex.common.casting.CastException.Reason.INVALID_TYPE -> "cannot use ${this.data[0]} as a SpellDatum (type ${this.data[0].javaClass.typeName})"
            at.petrak.hex.common.casting.CastException.Reason.OP_WRONG_TYPE -> "operator expected ${(this.data[0] as Class<*>).typeName} but got ${this.data[1]} (type ${this.data[1].javaClass.typeName})"
            at.petrak.hex.common.casting.CastException.Reason.NOT_ENOUGH_ARGS -> "required at least ${this.data[0] as Int} args on the stack but only had ${this.data[1] as Int}"
            at.petrak.hex.common.casting.CastException.Reason.TOO_FAR -> "tried to interact with something too far away at ${this.data[0] as Vec3}"
        }
}