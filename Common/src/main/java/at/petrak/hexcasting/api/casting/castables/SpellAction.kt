package at.petrak.hexcasting.api.casting.castables

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import net.minecraft.nbt.CompoundTag

interface SpellAction : Action {
    val argc: Int

    fun hasCastingSound(ctx: CastingEnvironment): Boolean = true

    fun awardsCastingStat(ctx: CastingEnvironment): Boolean = true

    fun execute(
        args: List<Iota>,
        ctx: CastingEnvironment
    ): Result

    fun executeWithUserdata(
        args: List<Iota>, ctx: CastingEnvironment, userData: CompoundTag
    ): Result {
        return this.execute(args, ctx)
    }

    override fun operate(
        env: CastingEnvironment,
        stack: MutableList<Iota>,
        userData: CompoundTag,
        continuation: SpellContinuation
    ): OperationResult {
        if (this.argc > stack.size)
            throw MishapNotEnoughArgs(this.argc, stack.size)
        val args = stack.takeLast(this.argc)
        for (_i in 0 until this.argc) stack.removeLast()
        val result = this.executeWithUserdata(args, env, userData)

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

        return OperationResult(stack, userData, sideEffects, continuation, result.opCount)
    }

    data class Result(val effect: RenderedSpell, val cost: Int, val particles: List<ParticleSpray>, val opCount: Long = 1)
}
