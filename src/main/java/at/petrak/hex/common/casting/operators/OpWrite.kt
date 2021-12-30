package at.petrak.hex.common.casting.operators

import at.petrak.hex.api.SimpleOperator
import at.petrak.hex.api.SpellOperator.Companion.spellListOf
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.SpellDatum
import at.petrak.hex.common.items.ItemSpellbook

object OpWriteToSpellbook : SimpleOperator {
    override val argc: Int
        get() = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val spellbook = ctx.getSpellbook()
        ItemSpellbook.WriteDatum(spellbook.orCreateTag, args[0])
        return spellListOf()
    }
}