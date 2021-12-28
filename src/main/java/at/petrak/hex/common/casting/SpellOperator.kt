package at.petrak.hex.common.casting

import at.petrak.hex.common.casting.operators.*
import at.petrak.hex.common.casting.operators.spells.OpAddMotion
import at.petrak.hex.common.casting.operators.spells.OpExplode
import at.petrak.hex.common.casting.operators.spells.OpPrint
import at.petrak.hex.hexmath.HexPattern
import net.minecraft.world.phys.Vec3

/**
 * Manipulates the stack in some way, usually by popping some number of values off the stack
 * and pushing one new value.
 * For a more "traditional" pop arguments, push return experience, see
 * [SimpleOperator][at.petrak.hex.common.casting.operators.SimpleOperator]
 *
 * Implementors MUST NOT mutate the context.
 */
interface SpellOperator {
    val manaCost: Int
        get() = 0

    fun modifyStack(stack: MutableList<SpellDatum<*>>, ctx: CastingContext)

    companion object {
        val PatternMap: Map<String, SpellOperator> = mapOf(
            // == Getters ==

            // diamond shape to get the caster
            "qaq" to OpGetCaster,
            "ede" to OpGetCaster,
            // small triangle to get the entity pos
            "aa" to OpEntityPos,
            "dd" to OpEntityPos,
            // Arrow to get the look vector
            "wa" to OpEntityLook,
            "wd" to OpEntityLook,

            // CCW battleaxe for block raycast
            "wqaawdd" to OpBlockRaycast,
            // and CW for axis raycast
            "weddwaa" to OpBlockAxisRaycast,
            // CCW diamond mace thing for entity raycast
            "weaqa" to OpEntityRaycast,

            // == Modify Stack ==

            // CCW hook for undo
            "a" to OpUndo,
            // and CW for null
            "d" to SpellWidget.NULL,
            // Two triangles holding hands to duplicate
            "aadaa" to OpDuplicate,
            // Two opposing triangles to swap
            "aawdd" to OpSwap,

            // == Spells ==

            // hook for debug
            "de" to OpPrint,
            "aq" to OpPrint,
            // nuclear sign for explosion
            "aawaawaa" to OpExplode,
            "weeewdq" to OpAddMotion,

            // == Meta stuff ==
            "qqq" to SpellWidget.OPEN_PAREN,
            "eee" to SpellWidget.CLOSE_PAREN,
            "qqqaw" to SpellWidget.ESCAPE,
            // http://www.toroidalsnark.net/mkss3-pix/CalderheadJMM2014.pdf
            // eval being a space filling curve feels apt doesn't it
            "deaqq" to OpEval,
            "aqqqqq" to OpReadFromSpellbook,
            "deeeee" to OpWriteToSpellbook,
        )

        /**
         * May throw CastException
         */
        fun fromPattern(pattern: HexPattern): SpellOperator =
            PatternMap.getOrElse(pattern.anglesSignature()) {
                throw CastException(CastException.Reason.INVALID_PATTERN, pattern)
            }

        // I see why vzakii did this: you can't raycast out to infinity!
        const val MAX_DISTANCE: Double = 32.0

        @JvmStatic
        fun raycastEnd(origin: Vec3, look: Vec3): Vec3 =
            origin.add(look.normalize().scale(MAX_DISTANCE))

        /**
         * Try to get a value of the given type.
         */
        @JvmStatic
        inline fun <reified T : Any> List<SpellDatum<*>>.getChecked(idx: Int): T =
            this[idx].tryGet()

        /**
         * Check if the value at the given index is OK. Will throw an error otherwise.
         */
        @JvmStatic
        inline fun <reified T : Any> List<SpellDatum<*>>.assertChecked(idx: Int) {
            this.getChecked<T>(idx)
        }

        /**
         * Make sure the vector is in range of the player.
         */
        @JvmStatic
        fun assertVecInRange(vec: Vec3, ctx: CastingContext) {
            if (vec.distanceToSqr(ctx.caster.position()) > MAX_DISTANCE * MAX_DISTANCE)
                throw CastException(CastException.Reason.TOO_FAR, vec)
        }

        @JvmStatic
        fun spellListOf(vararg vs: Any): List<SpellDatum<*>> {
            val out = ArrayList<SpellDatum<*>>(vs.size)
            for (v in vs) {
                out.add(SpellDatum.make(v))
            }
            return out
        }
    }

}