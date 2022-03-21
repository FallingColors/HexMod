package at.petrak.hexcasting.api.spell

import at.petrak.hexcasting.common.casting.CastingContext

interface RenderedSpell {
    fun cast(ctx: CastingContext)
}