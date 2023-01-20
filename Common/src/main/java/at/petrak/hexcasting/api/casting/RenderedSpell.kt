package at.petrak.hexcasting.api.casting

import at.petrak.hexcasting.api.casting.eval.CastingContext

interface RenderedSpell {
    fun cast(ctx: CastingContext)
}
