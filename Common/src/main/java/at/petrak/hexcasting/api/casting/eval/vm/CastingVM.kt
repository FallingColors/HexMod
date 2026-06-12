package at.petrak.hexcasting.api.casting.eval.vm

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.eval.*
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage.ParenthesizedIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.casting.mishaps.MishapEvalTooMuch
import at.petrak.hexcasting.api.casting.mishaps.MishapInternalException
import at.petrak.hexcasting.api.casting.mishaps.MishapStackSize
import at.petrak.hexcasting.api.utils.validateIota
import at.petrak.hexcasting.api.utils.validateIotaList
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

/**
 * The virtual machine! This is the glue that determines the next iteration of a [CastingImage], using a
 * [CastingEnvironment] to affect the world.
 */
class CastingVM(var image: CastingImage, val env: CastingEnvironment) {
    init {
        env.triggerCreateEvent(image.userData)
    }

    /**
     * Execute a single iota.
     */
    fun queueExecuteAndWrapIota(iota: Iota, world: ServerLevel): ExecutionClientView = queueExecuteAndWrapIotas(listOf(iota), world)

    /**
     * The main entrypoint to the VM. Given a list of iotas, execute them in sequence, and return whatever the client
     * needs to see.
     *
     * Mutates this
     */
    fun queueExecuteAndWrapIotas(iotas: List<Iota>, world: ServerLevel): ExecutionClientView {
        this.image = image.copy(
            stack = validateIotaList(this.image.stack, world)
        )
        // Initialize the continuation stack to a single top-level eval for all iotas.
        var continuation = SpellContinuation.Done.pushFrame(FrameEvaluate(SpellList.LList(0, iotas), false))
        // Begin aggregating info
        val info = TempControllerInfo(earlyExit = false)
        var lastResolutionType = ResolvedPatternType.UNRESOLVED
        while (continuation is SpellContinuation.NotDone && !info.earlyExit) {
            // Take the top of the continuation stack...
            val next = continuation.frame
            // ...and execute it.
            // TODO there used to be error checking code here; I'm pretty sure any and all mishaps should already
            // get caught and folded into CastResult by evaluate.
            val image2 = next.evaluate(continuation.next, world, this).let { result ->
                // if stack is unable to be serialized, have the result be an error
                if (result.newData != null && IotaType.isTooLargeToSerialize(result.newData.stack)) {
                    result.copy(
                        newData = null,
                        sideEffects = listOf(OperatorSideEffect.DoMishap(MishapStackSize(), Mishap.Context(null, null))),
                        resolutionType = ResolvedPatternType.ERRORED,
                        sound = HexEvalSounds.MISHAP,
                    )
                } else if (result.newData != null && result.newData.opsConsumed > env.maxOpCount()) {
                    result.copy(
                        newData = null,
                        sideEffects = listOf(OperatorSideEffect.DoMishap(MishapEvalTooMuch(), Mishap.Context(null, null))),
                        resolutionType = ResolvedPatternType.ERRORED,
                        sound = HexEvalSounds.MISHAP,
                    )
                } else {
                    result
                }
            }

            // Then write all pertinent data back to the VM for the next iteration.
            if (image2.newData != null) {
                this.image = image2.newData.copy(stack = validateIotaList(image2.newData.stack));
            }
            this.env.postExecution(image2)

            continuation = image2.continuation
            lastResolutionType = image2.resolutionType
            try {
                performSideEffects(image2.sideEffects)
            } catch (e: Exception) {
                e.printStackTrace()
                performSideEffects(listOf(OperatorSideEffect.DoMishap(MishapInternalException(e), Mishap.Context(null, null))))
            }
            info.earlyExit = info.earlyExit || !lastResolutionType.success
        }

        if (continuation is SpellContinuation.NotDone) {
            lastResolutionType =
                if (lastResolutionType.success) ResolvedPatternType.EVALUATED else ResolvedPatternType.ERRORED
        }

        var ravenmind: CompoundTag? = image.ravenmind().getOrNull()

        if (ravenmind != null) {
            val test = IotaType.TYPED_CODEC.parse<Tag?>(NbtOps.INSTANCE, ravenmind).getOrThrow()
            val newIota = validateIota(test, world)
            ravenmind = IotaType.TYPED_CODEC.encodeStart<Tag?>(NbtOps.INSTANCE, newIota).getOrThrow() as CompoundTag?
        }

        val isStackClear = image.stack.isEmpty() && image.parenCount == 0 && !image.escapeNext && ravenmind == null

        this.env.postCast(image)
        return ExecutionClientView(isStackClear, lastResolutionType, image.stack, ravenmind)
    }

    /**
     * this DOES NOT THROW THINGS
     */
    @Throws()
    fun executeInner(iota: Iota, world: ServerLevel, continuation: SpellContinuation): CastResult {
        try {
            // TODO we can have a special intro/retro sound
            // ALSO TODO need to add reader macro-style things

            // Handle single-iota escaping (ie via Consideration)
            // This is here rather than in Iota since this behavior should not be overriden.
            if (this.image.escapeNext) {
                val newImage: CastingImage
                if (this.image.parenCount > 0) {
                    // if we're inside parentheses, add the iota to the list with escaped set to true
                    val newParens = this.image.parenthesized.toMutableList()
                    newParens.add(ParenthesizedIota(iota, true))
                    newImage = this.image.copy(
                        escapeNext = false,
                        parenthesized = newParens
                    )
                } else {
                    // if we're not in parentheses, just push the iota to the stack
                    val newStack = this.image.stack.toMutableList()
                    newStack.add(iota)
                    newImage = this.image.copy(
                        stack = newStack,
                        escapeNext = false,
                    )
                }
                return CastResult(iota, continuation, newImage, listOf(), ResolvedPatternType.ESCAPED, HexEvalSounds.NORMAL_EXECUTE)
            }

            if (this.image.parenCount > 0) {
                // Handle parens escaping
                return iota.executeInParens(this, world, continuation)
            } else {
                // Handle normal execution behavior
                return iota.execute(this, world, continuation)
            }
        } catch (exception: Exception) {
            // This means something very bad has happened
            exception.printStackTrace()
            return CastResult(
                iota,
                continuation,
                null,
                listOf(
                    OperatorSideEffect.DoMishap(
                        MishapInternalException(exception),
                        Mishap.Context(
                            (iota as? PatternIota)?.pattern ?: HexPattern(HexDir.WEST),
                            null
                        )
                    )
                ),
                ResolvedPatternType.ERRORED,
                HexEvalSounds.MISHAP
            )
        }
    }

    /**
     * Execute the side effects of a pattern, updating our aggregated info.
     */
    fun performSideEffects(sideEffects: List<OperatorSideEffect>) {
        for (haskellProgrammersShakingandCryingRN in sideEffects) {
            haskellProgrammersShakingandCryingRN.performEffect(this)
        }
    }

    data class TempControllerInfo(
        var earlyExit: Boolean,
    )

    companion object {
        @JvmStatic
        fun empty(env: CastingEnvironment): CastingVM {
            return CastingVM(CastingImage(), env)
        }
    }
}
