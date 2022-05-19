package at.petrak.hexcasting.api.spell

import at.petrak.hexcasting.api.spell.casting.CastingContext

interface RenderedSpell {
    fun cast(ctx: CastingContext)
}
