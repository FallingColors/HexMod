package at.petrak.hexcasting.common.casting.operators.eval

import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.SpellContinuation

object OpHalt : Action {
    override fun operate(
        continuation: SpellContinuation,
        stack: MutableList<Iota>,
        local: Iota,
        ctx: CastingContext
    ): OperationResult {
        var newStack = stack.toList()
        var done = false
        var newCont = continuation
        while (!done && newCont is SpellContinuation.NotDone) {
            // Kotlin Y U NO destructuring assignment
            val newInfo = newCont.frame.breakDownwards(newStack)
            done = newInfo.first
            newStack = newInfo.second
            newCont = newCont.next
        }
        // if we hit no continuation boundaries (i.e. thoth/hermes exits), we've TOTALLY cleared the itinerary...
        if (!done) {
            // bomb the stack so we exit
            newStack = listOf()
        }

        return OperationResult(newCont, newStack, local, listOf())
    }
}
