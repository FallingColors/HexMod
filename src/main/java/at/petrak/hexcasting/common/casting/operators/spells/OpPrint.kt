package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.OperationResult
import at.petrak.hexcasting.api.Operator
import at.petrak.hexcasting.api.RenderedSpell
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import net.minecraft.Util
import net.minecraft.network.chat.TextComponent

object OpPrint : Operator {
    override fun operate(stack: MutableList<SpellDatum<*>>, ctx: CastingContext): OperationResult {
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