package at.petrak.hexcasting.api.casting.castables

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughMedia
import at.petrak.hexcasting.api.utils.Vector
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import net.minecraft.nbt.CompoundTag

interface SpellAction : Action {
    val argc: Int

    fun hasCastingSound(ctx: CastingEnvironment): Boolean = true

    fun awardsCastingStat(ctx: CastingEnvironment): Boolean = true

    fun execute(
        args: Vector<Iota>,
        env: CastingEnvironment
    ): Result

    fun executeWithUserdata(
        args: Vector<Iota>, env: CastingEnvironment, userData: CompoundTag
    ): Result {
        return this.execute(args, env)
    }

    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        val stack = Vector.VectorBuilder<Iota>()

        if (this.argc > image.stack.size)
            throw MishapNotEnoughArgs(this.argc, image.stack.size)
        val args = image.stack.takeRight(this.argc)
        stack.addAll(image.stack.dropRight(this.argc))

        // execute!
        val userDataMut = image.userData.copy()
        val result = this.executeWithUserdata(args, env, userDataMut)

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

        val image2 = image.copy(stack = stack.result(), opsConsumed = image.opsConsumed + result.opCount, userData = userDataMut)

        val sound = if (this.hasCastingSound(env)) HexEvalSounds.SPELL else HexEvalSounds.MUTE
        return OperationResult(image2, sideEffects, continuation, sound)
    }

    data class Result(val effect: RenderedSpell, val cost: Long, val particles: List<ParticleSpray>, val opCount: Long = 1)
}
