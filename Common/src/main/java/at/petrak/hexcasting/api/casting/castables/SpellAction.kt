package at.petrak.hexcasting.api.casting.castables

import at.petrak.hexcasting.api.casting.OperationResult
import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs

interface SpellAction : Action {
    val argc: Int

    fun hasCastingSound(ctx: CastingContext): Boolean = true

    fun awardsCastingStat(ctx: CastingContext): Boolean = true

    fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>>?

    override fun operate(
        continuation: SpellContinuation,
        stack: MutableList<Iota>,
        ravenmind: Iota?,
        ctx: CastingContext
    ): OperationResult {
        if (this.argc > stack.size)
            throw MishapNotEnoughArgs(this.argc, stack.size)
        val args = stack.takeLast(this.argc)
        for (_i in 0 until this.argc) stack.removeLast()
        val executeResult = this.execute(args, ctx) ?: return OperationResult(continuation, stack, ravenmind, listOf())
        val (spell, media, particles) = executeResult

        val sideEffects = mutableListOf<OperatorSideEffect>()

        if (media > 0)
            sideEffects.add(OperatorSideEffect.ConsumeMedia(media))

        sideEffects.add(
            OperatorSideEffect.AttemptSpell(
                spell,
                this.hasCastingSound(ctx),
                this.awardsCastingStat(ctx)
            )
        )

        for (spray in particles)
            sideEffects.add(OperatorSideEffect.Particles(spray))

        return OperationResult(continuation, stack, ravenmind, sideEffects)
    }

}
