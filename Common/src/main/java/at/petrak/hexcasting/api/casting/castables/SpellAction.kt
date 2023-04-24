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
    ): Triple<RenderedSpell, Int, List<ParticleSpray>>?

    fun executeWithUserdata(
        args: List<Iota>, ctx: CastingEnvironment, userData: CompoundTag
    ): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
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
        val executeResult = this.executeWithUserdata(args, env, userData)
            ?: return OperationResult(stack, userData, listOf(), continuation)
        val (spell, media, particles) = executeResult

        val sideEffects = mutableListOf<OperatorSideEffect>()

        if (media > 0)
            sideEffects.add(OperatorSideEffect.ConsumeMedia(media))

        sideEffects.add(
            OperatorSideEffect.AttemptSpell(
                spell,
                this.hasCastingSound(env),
                this.awardsCastingStat(env)
            )
        )

        for (spray in particles)
            sideEffects.add(OperatorSideEffect.Particles(spray))

        return OperationResult(stack, userData, sideEffects, continuation)
    }

}
