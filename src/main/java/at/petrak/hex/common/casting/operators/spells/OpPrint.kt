package at.petrak.hex.common.casting.operators.spells

import at.petrak.hex.api.RenderedSpell
import at.petrak.hex.api.SpellDatum
import at.petrak.hex.api.SpellOperator
import at.petrak.hex.common.casting.CastingContext
import net.minecraft.Util
import net.minecraft.network.chat.TextComponent

object OpPrint : SpellOperator {
    override val argc = 1
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Pair<RenderedSpell, Int> {
        return Pair(Spell(args[0]), 0)
    }

    private data class Spell(val datum: SpellDatum<*>) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            ctx.caster.sendMessage(
                TextComponent(datum.payload.toString()),
                Util.NIL_UUID
            )
        }
    }
}