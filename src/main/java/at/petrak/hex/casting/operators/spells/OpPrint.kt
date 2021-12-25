package at.petrak.hex.casting.operators.spells

import at.petrak.hex.casting.CastingContext
import at.petrak.hex.casting.RenderedSpell
import at.petrak.hex.casting.SpellDatum
import at.petrak.hex.casting.operators.SpellOperator
import at.petrak.hex.casting.operators.SpellOperator.Companion.spellListOf
import net.minecraft.Util
import net.minecraft.network.chat.TextComponent

object OpPrint : SpellOperator {
    override val argc = 1
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        return spellListOf(Spell(args[0]))
    }

    class Spell(private val datum: SpellDatum<*>) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            ctx.caster.sendMessage(
                TextComponent(if (datum.payload is Unit) "null" else datum.payload.toString()),
                Util.NIL_UUID
            )
        }
    }
}