package at.petrak.hex.common.casting

import at.petrak.hex.HexMod
import at.petrak.hex.api.PatternRegistry
import at.petrak.hex.api.RenderedSpell
import at.petrak.hex.api.SpellDatum
import at.petrak.hex.hexmath.HexPattern
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import kotlin.math.max

/**
 * Keeps track of a player casting a spell on the server.
 * It's stored as NBT on the wand.
 */
class CastingHarness private constructor(
    val stack: MutableList<SpellDatum<*>>,
    var parenCount: Int,
    var parenthesized: MutableList<HexPattern>,
    var escapeNext: Boolean,
    val ctx: CastingContext,
) {
    constructor(ctx: CastingContext) : this(mutableListOf(), 0, mutableListOf(), false, ctx)

    /**
     * When the server gets a packet from the client with a new pattern,
     * handle it.
     */
    fun update(newPat: HexPattern): CastResult {
        return try {
            var spellsToCast = emptyList<RenderedSpell>()
            var exn: CastException? = null
            val operator = try {
                PatternRegistry.lookupPattern(newPat)
            } catch (e: CastException) {
                exn = e
                null
            }
            if (this.parenCount > 0) {
                if (this.escapeNext) {
                    this.escapeNext = false
                    this.parenthesized.add(newPat)
                    HexMod.LOGGER.info("Escaping onto parenthesized")
                } else if (operator == Widget.ESCAPE) {
                    this.escapeNext = true
                } else if (operator == Widget.OPEN_PAREN) {
                    // we have escaped the parens onto the stack; we just also record our count.
                    this.parenthesized.add(newPat)
                    this.parenCount++
                } else if (operator == Widget.CLOSE_PAREN) {
                    this.parenCount--
                    if (this.parenCount == 0) {
                        HexMod.LOGGER.info("Finished parenthesizing things")
                        this.stack.add(SpellDatum.make(this.parenthesized.map { SpellDatum.make(it) }))
                    } else if (this.parenCount < 0) {
                        throw CastException(CastException.Reason.TOO_MANY_CLOSE_PARENS)
                    } else {
                        // we have this situation: "(()"
                        // we need to add the close paren
                        this.parenthesized.add(newPat)
                    }
                } else {
                    this.parenthesized.add(newPat)
                }
            } else if (this.escapeNext) {
                this.escapeNext = false
                HexMod.LOGGER.info("Escaping onto stack")
                this.stack.add(SpellDatum.make(newPat))
            } else if (operator == Widget.ESCAPE) {
                this.escapeNext = true
            } else if (exn != null) {
                // there was a problem finding the pattern and it was NOT due to numbers
                throw exn
            } else if (operator == Widget.OPEN_PAREN) {
                this.parenCount++
            } else if (operator == Widget.CLOSE_PAREN) {
                throw CastException(CastException.Reason.TOO_MANY_CLOSE_PARENS)
            } else {
                // we know the operator is ok here
                val (manaCost, spells) = operator!!.modifyStack(this.stack, this.ctx)
                spellsToCast = spells

                // prevent poor impls from gaining you mana
                ctx.withdrawMana(max(0, manaCost), true)
                if (ctx.caster.isDeadOrDying)
                    return CastResult.Died
            }

            if (this.parenCount > 0) {
                HexMod.LOGGER.info("Paren level ${this.parenCount}; ${this.parenthesized}")
            }
            HexMod.LOGGER.info("New stack: ${this.stack.map { it.display() }}")

            if (spellsToCast.isNotEmpty()) {
                CastResult.Cast(spellsToCast, this.stack.isEmpty())
            } else if (this.stack.isEmpty()) {
                if (this.parenCount == 0) {
                    CastResult.QuitCasting
                } else {
                    CastResult.Nothing
                }
            } else {
                CastResult.Nothing
            }
        } catch (e: CastException) {
            CastResult.Error(e)
        }
    }

    fun serializeToNBT(): CompoundTag {
        val out = CompoundTag()

        val stackTag = ListTag()
        for (datum in this.stack)
            stackTag.add(datum.serializeToNBT())
        out.put(TAG_STACK, stackTag)

        out.putInt(TAG_PAREN_COUNT, this.parenCount)
        out.putBoolean(TAG_ESCAPE_NEXT, this.escapeNext)

        val parensTag = ListTag()
        for (pat in this.parenthesized)
            parensTag.add(pat.serializeToNBT())
        out.put(TAG_PARENTHESIZED, parensTag)

        return out
    }

    companion object {
        const val TAG_STACK = "stack"
        const val TAG_PAREN_COUNT = "open_parens"
        const val TAG_PARENTHESIZED = "parenthesized"
        const val TAG_ESCAPE_NEXT = "escape_next"

        @JvmStatic
        fun DeserializeFromNBT(nbt: Tag?, caster: ServerPlayer, wandHand: InteractionHand): CastingHarness {
            val ctx = CastingContext(caster, wandHand)
            return try {
                val nbt = nbt as CompoundTag

                val stack = mutableListOf<SpellDatum<*>>()
                val stackTag = nbt.getList(TAG_STACK, Tag.TAG_COMPOUND.toInt())
                for (subtag in stackTag) {
                    val datum = SpellDatum.DeserializeFromNBT(subtag as CompoundTag, ctx)
                    stack.add(datum)
                }

                val parenthesized = mutableListOf<HexPattern>()
                val parenTag = nbt.getList(TAG_PARENTHESIZED, Tag.TAG_COMPOUND.toInt())
                for (subtag in parenTag) {
                    parenthesized.add(HexPattern.DeserializeFromNBT(subtag as CompoundTag))
                }

                val parenCount = nbt.getInt(TAG_PAREN_COUNT)
                val escapeNext = nbt.getBoolean(TAG_ESCAPE_NEXT)

                CastingHarness(stack, parenCount, parenthesized, escapeNext, ctx)
            } catch (exn: Exception) {
                HexMod.LOGGER.warn("Couldn't load harness from nbt tag, falling back to default: $nbt: $exn")
                CastingHarness(ctx)
            }
        }
    }

    sealed class CastResult {
        /** Casting still in progress */
        object Nothing : CastResult()

        /** Non-catastrophic quit */
        object QuitCasting : CastResult()

        /** Finished casting */
        data class Cast(val spells: List<RenderedSpell>, val quit: Boolean) : CastResult()

        /** uh-oh */
        data class Error(val exn: CastException) : CastResult()

        /** YOU DIED due to casting too hard from hit points. */
        object Died : CastResult()

        fun shouldQuit(): Boolean =
            when (this) {
                QuitCasting, Died, is Error -> true
                is Cast -> this.quit
                else -> false
            }
    }
}