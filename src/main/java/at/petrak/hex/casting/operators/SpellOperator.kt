package at.petrak.hex.casting.operators

import at.petrak.hex.casting.CastException
import at.petrak.hex.casting.CastingContext
import at.petrak.hex.casting.SpellDatum
import at.petrak.hex.casting.operators.spells.OpPrint
import at.petrak.hex.hexes.HexPattern
import net.minecraft.world.phys.Vec3

/**
 * Manipulates the stack in some way, usually by popping some number of values off the stack
 * and pushing one new value.
 *
 * Implementors MUST NOT mutate the stack or the context.
 */
interface SpellOperator {
    val argc: Int
    val manaCost: Int
        get() = 0

    fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>>

    companion object {
        val PatternMap: Map<String, SpellOperator> = mapOf(
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

            // ===

            // hook for debug
            "de" to OpPrint,
            "aq" to OpPrint,
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
        inline fun <reified T : Any> List<SpellDatum<*>>.getChecked(idx: Int): T {
            val datum = this[idx]
            val casted = datum.tryGet<T>()
            return if (casted is Double && !casted.isFinite())
                0.0 as T
            else
                casted
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