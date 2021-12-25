package at.petrak.hex.casting

/**
 * The result of a spell being cast.
 *
 * A "spell" is just a [SpellOperator] that returns a [RenderedSpell],
 * Once a spell stack has nothing on it but a single [RenderedSpell], the casting is nearly successful.
 * If the caster has enough mana to cast the [RenderedSpell], it is cast! (Otherwise, we might cast from
 * hitpoints or just kill the caster.)
 */
fun interface RenderedSpell {
    fun cast(ctx: CastingContext)
}