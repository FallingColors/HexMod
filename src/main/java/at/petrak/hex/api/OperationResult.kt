package at.petrak.hex.api

/**
 * What happens when an operator is through?
 *
 * This has the mana cost and some spells that might be cast.
 */
data class OperationResult(val manaCost: Int, val spells: List<RenderedSpell>)
