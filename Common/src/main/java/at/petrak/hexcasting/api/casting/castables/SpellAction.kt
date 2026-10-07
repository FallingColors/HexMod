package at.petrak.hexcasting.api.casting.castables

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughMedia
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import net.minecraft.nbt.CompoundTag

interface SpellAction : Action {
    val argc: Int

    fun hasCastingSound(env: CastingEnvironment): Boolean = true

    fun awardsCastingStat(env: CastingEnvironment): Boolean = true

    @Throws(Mishap::class)
    fun execute(
        args: List<Iota>,
        env: CastingEnvironment
    ): Result

    /**
     * Override this rather than [execute] if you need to read data from the [CastingImage] while setting up for your
     * spell. Note that you cannot *update* the [CastingImage] with this - if you need to do that, pass the relevant
     * data into your [RenderedSpell] implementation and use the [RenderedSpell.cast] overload that returns a new image.
     */
    @Throws(Mishap::class)
    fun executeWithImage(
        args: List<Iota>, env: CastingEnvironment, image: CastingImage
    ): Result {
        return this.execute(args, env)
    }

    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        val stack = image.stack

        if (this.argc > stack.size)
            throw MishapNotEnoughArgs(this.argc, stack.size)
        val args = stack.takeRight(this.argc)
        val stackWithoutArgs = stack.dropRight(this.argc)

        // execute!
        val result = this.executeWithImage(args, env, image.copy(stack = stack))

        val sideEffects = mutableListOf<OperatorSideEffect>()

        if (env.extractMedia(result.cost, true) > 0)
            throw MishapNotEnoughMedia(result.cost)
        if (result.cost > 0)
            sideEffects.add(OperatorSideEffect.ConsumeMedia(result.cost))

        sideEffects.add(
            OperatorSideEffect.AttemptSpell(
                result.effect,
                this.hasCastingSound(env),
                this.awardsCastingStat(env)
            )
        )

        for (spray in result.particles)
            sideEffects.add(OperatorSideEffect.Particles(spray))

        val image2 = image.copy(stack = stackWithoutArgs, opsConsumed = image.opsConsumed + result.opCount)

        val sound = if (this.hasCastingSound(env)) HexEvalSounds.SPELL.get() else HexEvalSounds.MUTE.get()
        return OperationResult(image2, sideEffects, continuation, sound)
    }

    data class Result(val effect: RenderedSpell, val cost: Long, val particles: List<ParticleSpray>, val opCount: Long = 1)
}
