package at.petrak.hex.api

import at.petrak.hex.common.casting.CastingContext

interface RenderedSpell {
    fun cast(ctx: CastingContext)
}