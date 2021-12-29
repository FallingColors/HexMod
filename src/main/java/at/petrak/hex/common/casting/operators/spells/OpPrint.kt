package at.petrak.hex.common.casting.operators.spells

import at.petrak.hex.api.SimpleOperator
import at.petrak.hex.api.SpellOperator.Companion.getChecked
import at.petrak.hex.api.SpellOperator.Companion.spellListOf
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.RenderedSpell
import at.petrak.hex.common.casting.RenderedSpellImpl
import at.petrak.hex.common.casting.SpellDatum
import net.minecraft.Util
import net.minecraft.network.chat.TextComponent

object OpPrint : SimpleOperator, RenderedSpellImpl {
    override val argc = 1
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val datum = args.getChecked<Any>(0)
        return spellListOf(RenderedSpell(OpPrint, spellListOf(datum)))
    }

    override fun cast(args: List<SpellDatum<*>>, ctx: CastingContext) {
        val datum = args[0]
        ctx.caster.sendMessage(
            TextComponent(datum.payload.toString()),
            Util.NIL_UUID
        )
    }
}