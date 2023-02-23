package at.petrak.hexcasting.api.casting

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment

interface RenderedSpell {
    fun cast(ctx: CastingEnvironment)
}
