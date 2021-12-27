package at.petrak.hex.common.casting

/**
 * The implementation of a spellcast.
 *
 * A "spell" is just a [SpellOperator] that returns a [RenderedSpellImpl],
 * Once a spell stack has nothing on it but a single [RenderedSpellImpl], the casting is nearly successful.
 * If the caster has enough mana to cast the [RenderedSpellImpl], it is cast! (Otherwise, we might cast from
 * hitpoints or just kill the caster.)
 *
 * This contains just the code for what to do. Due to *ahem* issues with Java, we can't have an abstract
 * constructor from NBT. So, we encode a RenderedSpell as a string key plus a list of SpellDatum arguments.
 * When a [RenderedSpellImpl] is successfully returned, we stick it inside a RenderedSpell with its data.
 * The implementation is responsible for re-de-serializing the data out of the list and casting the spell.
 */
interface RenderedSpellImpl {
    fun cast(args: List<SpellDatum<*>>, ctx: CastingContext)
}