package at.petrak.hex.common.casting

@JvmRecord
data class RenderedSpell(val spell: RenderedSpellImpl, val args: List<SpellDatum<*>>) {
    fun cast(ctx: CastingContext) {
        this.spell.cast(args, ctx)
    }
}