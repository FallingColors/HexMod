package at.petrak.hexcasting.api.casting.eval

import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.nbt.CompoundTag

/**
 * What happens when an operator is through?
 */
data class OperationResult(
    val newStack: List<Iota>,
    val newUserdata: CompoundTag,
    val sideEffects: List<OperatorSideEffect>,
    val newContinuation: SpellContinuation,
    val opsUsed: Long,
)
