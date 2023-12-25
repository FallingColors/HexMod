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
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import net.minecraft.nbt.CompoundTag

interface SpellAction : Action {
    val argc: Int

    fun hasCastingSound(ctx: CastingEnvironment): Boolean = true

    fun awardsCastingStat(ctx: CastingEnvironment): Boolean = true

    fun execute(
        args: List<Iota>,
        env: CastingEnvironment
    ): Result

    fun executeWithUserdata(
        args: List<Iota>, env: CastingEnvironment, userData: CompoundTag
    ): Result {
        return this.execute(args, env)
    }

    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        val stack = image.stack.toMutableList()

        if (this.argc > stack.size)
            throw MishapNotEnoughArgs(this.argc, stack.size)
        val args = stack.takeLast(this.argc)
        for (_i in 0 until this.argc) stack.removeLast()

        // execute!
        val userDataMut = image.userData.copy()
        val result = this.executeWithUserdata(args, env, userDataMut)

        val sideEffects = mutableListOf<OperatorSideEffect>()

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

        val image2 = image.copy(stack = stack, opsConsumed = image.opsConsumed + result.opCount, userData = userDataMut)

        val sound = if (this.hasCastingSound(env)) HexEvalSounds.SPELL else HexEvalSounds.MUTE
        return OperationResult(image2, sideEffects, continuation, sound)
    }

    data class Result(val effect: RenderedSpell, val cost: Long, val particles: List<ParticleSpray>, val opCount: Long = 1)
}
