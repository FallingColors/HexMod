package at.petrak.hexcasting.api.casting

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage

interface RenderedSpell {
    fun cast(env: CastingEnvironment)

    fun cast(env: CastingEnvironment, image: CastingImage): CastingImage? {
        cast(env)
        return null
    }
}
