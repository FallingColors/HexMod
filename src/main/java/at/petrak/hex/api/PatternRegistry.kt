package at.petrak.hex.api

import at.petrak.hex.common.casting.CastException
import at.petrak.hex.hexmath.HexPattern
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ConcurrentMap

/**
 * Register your patterns and associate them with spells here.
 *
 * Most patterns are matches to their operators solely by their *angle signature*.
 * This is a record of all the angles in the pattern, independent of the direction the player started drawing in.
 * It's written in shorthand, where `w` is straight ahead, `q` is left, `a` is left-back, `d` is right-back,
 * and `e` is right.
 *
 * For example, the signature for a straight line of 3 segments is `"ww"`. The signatures for the "get caster"
 * operator (the diamond) are `"qaq"` and `"ede"`.
 */
object PatternRegistry {
    private val regularPatterns: ConcurrentMap<String, Operator> = ConcurrentHashMap()
    private val specialHandlers: ConcurrentLinkedDeque<SpecialHandler> = ConcurrentLinkedDeque()


    /**
     * Associate a given angle signature with a SpellOperator.
     */
    @JvmStatic
    @Throws(RegisterPatternException::class)
    fun addRegularPattern(signature: String, operator: Operator) {
        if (this.regularPatterns.containsKey(signature))
            throw RegisterPatternException("The signature `$signature` already exists")
        this.regularPatterns[signature] = operator
    }

    /**
     * Associate a given angle signature and its mirror image with a SpellOperator.
     */
    @JvmStatic
    @Throws(RegisterPatternException::class)
    fun addRegularPatternAndMirror(signature: String, operator: Operator) {
        this.addRegularPattern(signature, operator)
        val flipped = mirrorSig(signature)
        if (flipped != signature) {
            this.addRegularPattern(signature, operator)
        }
    }

    /**
     * Add a special handler, to take an arbitrary pattern and return whatever kind of operator you like.
     */
    @JvmStatic
    fun addSpecialHandler(handler: SpecialHandler) {
        this.specialHandlers.add(handler)
    }

    fun lookupPattern(pat: HexPattern): Operator {
        for (handler in specialHandlers) {
            val op = handler.handlePattern(pat)
            if (op != null) return op
        }
        val sig = pat.anglesSignature()
        return this.regularPatterns.getOrElse(sig) {
            throw CastException(CastException.Reason.INVALID_PATTERN, pat)
        }
    }

    /**
     * Special handling of a pattern. Before checking any of the normal angle-signature based patterns,
     * a given pattern is run by all of these special handlers patterns. If none of them return non-null,
     * then its signature is checked.
     *
     * In the base mod, this is used for number patterns.
     */
    fun interface SpecialHandler {
        fun handlePattern(pattern: HexPattern): Operator?
    }

    private fun mirrorSig(sig: String): String = buildString {
        for (c in sig.chars()) {
            append(
                when (c.toChar()) {
                    'q' -> 'e'
                    'a' -> 'd'
                    'e' -> 'q'
                    'd' -> 'a'
                    else -> c
                }
            )
        }
    }

    class RegisterPatternException(msg: String) : java.lang.Exception(msg)
}