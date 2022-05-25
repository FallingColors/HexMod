package at.petrak.hexcasting.common.casting.operators.math.logic

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext

object OpBoolIdentityKindOf : ConstManaOperator {
    override val argc = 1

    override fun execute(args: List<LegacySpellDatum<*>>, ctx: CastingContext): List<LegacySpellDatum<*>> {
        val payload = args[0].payload

        if (payload == Widget.NULL)
            return 0.asSpellResult

        if (payload.tolerantEquals(0.0) || payload.tolerantEquals(1.0))
            return null.asSpellResult

        if (payload is SpellList)
            return if (payload.nonEmpty) payload.asSpellResult else null.asSpellResult

        return spellListOf(payload)
    }
}
