package at.petrak.hexcasting.api.casting

import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.phys.Vec3
import java.text.DecimalFormat

/**
 * Manipulates the stack in some way, usually by popping some number of values off the stack
 * and pushing one new value.
 * For a more "traditional" pop arguments, push return experience, see [ConstMediaAction].
 *
 * Instances of this can exist on the client, but they should NEVER be used there. They only
 * exist on the client because Minecraft's registry system demands they do; any information
 * the client needs about them is stored elsewhere. (For example, their canonical stroke order
 * is stored in [ActionRegistryEntry], and their localization key is gotten from the resource key
 * via [at.petrak.hexcasting.api.HexAPI.getActionI18nKey].)
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

    companion object {
        // I see why vzakii did this: you can't raycast out to infinity!
        const val MAX_DISTANCE: Double = 32.0
        const val MAX_DISTANCE_FROM_SENTINEL: Double = 16.0

        @JvmStatic
        fun raycastEnd(origin: Vec3, look: Vec3): Vec3 =
            origin.add(look.normalize().scale(MAX_DISTANCE))

        @JvmStatic
        fun makeConstantOp(x: Iota): Action = object : ConstMediaAction {
            override val argc: Int
                get() = 0

            override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> =
                listOf(x)
        }

        public val DOUBLE_FORMATTER = DecimalFormat("####.####")
    }
}

