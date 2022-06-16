package at.petrak.hexcasting.api.spell

import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.SpellContinuation
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.world.phys.Vec3

/**
 * Manipulates the stack in some way, usually by popping some number of values off the stack
 * and pushing one new value.
 * For a more "traditional" pop arguments, push return experience, see [ConstManaAction].
 *
 * Implementors MUST NOT mutate the context.
 */
interface Action {
    /**
     * Operate on the stack. Return the new stack and any side effects of the cast.
     *
     * Although this is passed a [MutableList], this is only for the convenience of implementors.
     * It is a clone of the stack and modifying it does nothing. You must return the new stack
     * with the [OperationResult].
     *
     * A particle effect at the cast site and various messages and advancements are done automagically.
     */
    fun operate(
        continuation: SpellContinuation,
        stack: MutableList<Iota>,
        ravenmind: Iota?,
        ctx: CastingContext
    ): OperationResult

    /**
     * Do you need to be enlightened to use this operator? (i.e. is this operator a Great Pattern)
     */
    val isGreat: Boolean get() = false

    /**
     * Should this Great Pattern process and have side effects, even if its user isn't enlightened?
     *
     * The pattern itself may modify its effects based on whether the user is enlightened or not, regardless of what this value is.
     */
    val alwaysProcessGreatSpell: Boolean get() = this is SpellAction

    /**
     * Can this Great Pattern give you Blind Diversion?
     */
    val causesBlindDiversion: Boolean get() = this is SpellAction

    companion object {
        // I see why vzakii did this: you can't raycast out to infinity!
        const val MAX_DISTANCE: Double = 32.0
        const val MAX_DISTANCE_FROM_SENTINEL: Double = 16.0

        @JvmStatic
        fun raycastEnd(origin: Vec3, look: Vec3): Vec3 =
            origin.add(look.normalize().scale(MAX_DISTANCE))

        @JvmStatic
        fun makeConstantOp(x: Iota): Action = object : ConstManaAction {
            override val argc: Int
                get() = 0

            override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> =
                listOf(x)
        }
    }
}

