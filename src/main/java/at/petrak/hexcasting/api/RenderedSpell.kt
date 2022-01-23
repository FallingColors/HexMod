package at.petrak.hexcasting.api

import at.petrak.hexcasting.common.casting.CastingContext

interface RenderedSpell {
    fun cast(ctx: CastingContext)
}