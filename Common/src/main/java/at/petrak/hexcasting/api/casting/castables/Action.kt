package at.petrak.hexcasting.api.casting.castables

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
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
 *
 * Each action is a singleton
 */
interface Action {
    /**
     * Functionally update the image. Return the image and any side effects.
     *
     * This is a <i>very</i> low-level function -- the implementor is responsible for a lot. For example,
     * remember to increment the op count, sil vous plait.
     *
     * A particle effect at the cast site and various messages and advancements are done automagically.
     *
     * The userdata tag is copied for you, so you don't need to worry about mutation messing up things
     * behind the scenes.
     */
    fun operate(
        env: CastingEnvironment,
        image: CastingImage,
        continuation: SpellContinuation
    ): OperationResult

    companion object {
        // I see why vazkii did this: you can't raycast out to infinity!
        const val RAYCAST_DISTANCE: Double = 32.0

        // TODO: currently, this means you can't raycast in a very long spell circle, or out of your local ambit into
        // your sentinel's.
        @JvmStatic
        fun raycastEnd(origin: Vec3, look: Vec3): Vec3 =
            origin.add(look.normalize().scale(RAYCAST_DISTANCE))

        @JvmStatic
        fun makeConstantOp(x: Iota): Action = object : ConstMediaAction {
            override val argc: Int
                get() = 0

            override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> =
                listOf(x)
        }

        public val DOUBLE_FORMATTER = DecimalFormat("####.####")
    }
}

