package at.petrak.hex.common.casting.operators.spells

import at.petrak.hex.api.OperationResult
import at.petrak.hex.api.Operator
import at.petrak.hex.api.RenderedSpell
import at.petrak.hex.api.SpellDatum
import at.petrak.hex.common.casting.CastingContext
import net.minecraft.Util
import net.minecraft.network.chat.TextComponent

object OpPrint : Operator {
    override fun modifyStack(stack: MutableList<SpellDatum<*>>, ctx: CastingContext): OperationResult {
        val datum = stack[stack.lastIndex]
        return OperationResult(0, listOf(Spell(datum)))
    }

    private data class Spell(val datum: SpellDatum<*>) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            ctx.caster.sendMessage(
                TextComponent(datum.display()),
                Util.NIL_UUID
            )
        }
    }
}