package at.petrak.hex.common.casting

import at.petrak.hex.HexMod
import at.petrak.hex.hexmath.HexPattern
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerPlayer

/**
 * Keeps track of a player casting a spell on the server.
 * It's stored as NBT on the wand.
 */
class CastingHarness private constructor(
    val stack: MutableList<SpellDatum<*>>,
    val ctx: CastingContext,
) {
    /**
     * When the server gets a packet from the client with a new pattern,
     * handle it.
     */
    fun update(newPat: HexPattern): CastResult {
        return try {
            val operator = SpellOperator.fromPattern(newPat)
            HexMod.LOGGER.info("Executing operator: $operator")
            operator.modifyStack(this.stack, this.ctx)
            HexMod.LOGGER.info("Modified stack: ${this.stack}")

            if (this.stack.isEmpty()) {
                return CastResult.QuitCasting
            }
            val maybeSpell = this.stack[0]
            if (this.stack.size == 1 && maybeSpell.payload is RenderedSpell) {
                CastResult.Success(maybeSpell.payload)
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

        val pointsTag = ListTag()
        out.put(TAG_POINTS, pointsTag)

        return out
    }

    companion object {
        const val TAG_STACK = "stack"
        const val TAG_POINTS = "points"

        @JvmStatic
        fun DeserializeFromNBT(nbt: Tag?, caster: ServerPlayer): CastingHarness {
            val ctx = CastingContext(caster)
            return try {
                val nbt = nbt as CompoundTag

                val stack = mutableListOf<SpellDatum<*>>()
                val stackTag = nbt.getList(TAG_STACK, Tag.TAG_COMPOUND.toInt())
                for (subtag in stackTag) {
                    val datum = SpellDatum.DeserializeFromNBT(subtag as CompoundTag, ctx)
                    stack.add(datum)
                }

                CastingHarness(stack, ctx)
            } catch (exn: Exception) {
                HexMod.LOGGER.warn("Couldn't load harness from nbt tag, falling back to default: $nbt: $exn")
                CastingHarness(mutableListOf(), ctx)
            }
        }
    }

    sealed class CastResult {
        /** Casting still in progress */
        object Nothing : CastResult()

        /** Non-catastrophic quit */
        object QuitCasting : CastResult()

        /** Finished casting */
        data class Success(val spell: RenderedSpell) : CastResult()

        /** uh-oh */
        data class Error(val exn: CastException) : CastResult()
    }
}