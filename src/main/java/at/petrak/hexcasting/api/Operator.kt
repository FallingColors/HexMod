package at.petrak.hexcasting.api

import at.petrak.hexcasting.common.casting.CastException
import at.petrak.hexcasting.common.casting.CastingContext
import net.minecraft.world.phys.Vec3
import kotlin.math.abs

/**
 * Manipulates the stack in some way, usually by popping some number of values off the stack
 * and pushing one new value.
 * For a more "traditional" pop arguments, push return experience, see
 * [SimpleOperator][at.petrak.hexcasting.common.casting.operators.SimpleOperator]
 *
 * Implementors MUST NOT mutate the context.
 */
interface Operator {
    /**
     * Operate on the stack. Return the new stack and any side effects of the cast.
     *
     * Although this is passed a [MutableList], this is only for the convenience of implementors.
     * It is a clone of the stack and modifying it does nothing. You must return the new stack
     * with the [OperationResult].
     *
     * A particle effect at the cast site and various messages and advancements are done automagically.
     */
    fun operate(stack: MutableList<SpellDatum<*>>, ctx: CastingContext): OperationResult

    /**
     * Do you need to be enlightened to use this operator?
     */
    val isGreat: Boolean get() = false

    companion object {
        // I see why vzakii did this: you can't raycast out to infinity!
        const val MAX_DISTANCE: Double = 32.0
        const val MAX_DISTANCE_FROM_SENTINEL: Double = 16.0

        @JvmStatic
        fun raycastEnd(origin: Vec3, look: Vec3): Vec3 =
            origin.add(look.normalize().scale(MAX_DISTANCE))

        /**
         * Try to get a value of the given type.
         */
        @JvmStatic
        inline fun <reified T : Any> List<SpellDatum<*>>.getChecked(idx: Int): T {
            val x = this.getOrElse(idx) { throw CastException(CastException.Reason.NOT_ENOUGH_ARGS, abs(idx), this.size) }
            return x.tryGet()
        }

        /**
         * Check if the value at the given index is OK. Will throw an error otherwise.
         */
        @JvmStatic
        inline fun <reified T : Any> List<SpellDatum<*>>.assertChecked(idx: Int) {
            this.getChecked<T>(idx)
        }

        @JvmStatic
        fun spellListOf(vararg vs: Any): List<SpellDatum<*>> {
            val out = ArrayList<SpellDatum<*>>(vs.size)
            for (v in vs) {
                out.add(SpellDatum.make(v))
            }
            return out
        }

        @JvmStatic
        fun makeConstantOp(x: SpellDatum<*>): Operator = object : ConstManaOperator {
            override val argc: Int
                get() = 0

            override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> =
                listOf(x)
        }
    }
}