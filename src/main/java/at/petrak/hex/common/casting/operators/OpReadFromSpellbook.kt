package at.petrak.hex.common.casting.operators

import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.SpellDatum
import at.petrak.hex.common.casting.SpellWidget
import at.petrak.hex.common.items.ItemSpellbook

object OpReadFromSpellbook : SimpleOperator {
    override val argc: Int
        get() = 0

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val spellbook = ctx.getSpellbook()
        val datum = ItemSpellbook.ReadDatum(spellbook.orCreateTag, ctx)
        return listOf(datum ?: SpellDatum.make(SpellWidget.NULL))
    }
}