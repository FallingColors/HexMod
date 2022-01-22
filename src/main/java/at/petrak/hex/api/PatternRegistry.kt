package at.petrak.hex.api

import at.petrak.hex.common.casting.CastException
import at.petrak.hex.hexmath.EulerPathFinder
import at.petrak.hex.hexmath.HexPattern
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.saveddata.SavedData
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ConcurrentMap

/**
 * Register your patterns and associate them with spells here.
 *
 * Most patterns are matches to their operators solely by their *angle signature*.
 * This is a record of all the angles in the pattern, independent of the direction the player started drawing in.
 * It's written in shorthand, where `w` is straight ahead, `q` is left, `a` is left-back, `d` is right-back,
 * and `e` is right.
 *
 * For example, the signature for a straight line of 3 segments is `"ww"`. The signatures for the "get caster"
 * operator (the diamond) are `"qaq"` and `"ede"`.
 */
object PatternRegistry {
    private val regularPatterns: ConcurrentMap<String, Operator> = ConcurrentHashMap()
    private val perWorldPatterns: ConcurrentMap<ResourceLocation, Pair<HexPattern, Operator>> = ConcurrentHashMap()
    private val specialHandlers: ConcurrentLinkedDeque<SpecialHandler> = ConcurrentLinkedDeque()


    /**
     * Associate a given angle signature with a SpellOperator.
     */
    @JvmStatic
    @Throws(RegisterPatternException::class)
    fun addRegularPattern(signature: String, operator: Operator) {
        if (this.regularPatterns.containsKey(signature))
            throw RegisterPatternException("The signature `$signature` already exists")
        this.regularPatterns[signature] = operator
    }

    /**
     * Associate a given angle signature and its mirror image with a SpellOperator.
     */
    @JvmStatic
    @Throws(RegisterPatternException::class)
    fun addRegularPatternAndMirror(signature: String, operator: Operator) {
        this.addRegularPattern(signature, operator)
        val flipped = mirrorSig(signature)
        if (flipped != signature) {
            this.addRegularPattern(flipped, operator)
        }
    }

    /**
     * Associate a given *pattern shape* with a SpellOperator.
     * The pattern will be different per world, but it will have the same shape.
     * (Different stroke order, same look.)
     *
     * Also, give it some kind of unique name so that it can be recalled from SavedData later.
     */
    @JvmStatic
    @Throws(RegisterPatternException::class)
    fun addRegularPatternPerWorld(pattern: HexPattern, id: ResourceLocation, operator: Operator) {
        if (this.perWorldPatterns.containsKey(id)) {
            throw RegisterPatternException("The id `$id` already exists")
        }

        this.perWorldPatterns[id] = Pair(pattern, operator)
    }

    /**
     * Add a special handler, to take an arbitrary pattern and return whatever kind of operator you like.
     */
    @JvmStatic
    fun addSpecialHandler(handler: SpecialHandler) {
        this.specialHandlers.add(handler)
    }

    /**
     * Internal use only.
     */
    @JvmStatic
    fun lookupPattern(pat: HexPattern, overworld: ServerLevel): Operator {
        // Pipeline:
        // patterns are registered here every time the game boots
        // when we try to look
        for (handler in specialHandlers) {
            val op = handler.handlePattern(pat)
            if (op != null) return op
        }

        // Is it global?
        val sig = pat.anglesSignature()
        this.regularPatterns[sig]?.let { return it }

        // Look it up in the world?
        val ds = overworld.dataStorage
        val perWorldPatterns: Save =
            ds.computeIfAbsent(Save.Companion::load, { Save.create(overworld.seed) }, TAG_SAVED_DATA)
        perWorldPatterns.lookup[sig]?.let { return this.perWorldPatterns[it]!!.second }

        throw CastException(CastException.Reason.INVALID_PATTERN, pat)
    }

    /**
     * Internal use only.
     */
    @JvmStatic
    fun getPerWorldPatterns(overworld: ServerLevel): Map<String, ResourceLocation> {
        val ds = overworld.dataStorage
        val perWorldPatterns: Save =
            ds.computeIfAbsent(Save.Companion::load, { Save.create(overworld.seed) }, TAG_SAVED_DATA)
        return perWorldPatterns.lookup
    }

    /**
     * Internal use only.
     */
    @JvmStatic
    fun lookupPerWorldPattern(opId: ResourceLocation): Pair<HexPattern, Operator> =
        this.perWorldPatterns.getOrElse(opId) {
            throw IllegalArgumentException("could not find a pattern for $opId")
        }

    /**
     * Special handling of a pattern. Before checking any of the normal angle-signature based patterns,
     * a given pattern is run by all of these special handlers patterns. If none of them return non-null,
     * then its signature is checked.
     *
     * In the base mod, this is used for number patterns.
     */
    fun interface SpecialHandler {
        fun handlePattern(pattern: HexPattern): Operator?
    }

    private fun mirrorSig(sig: String): String = buildString {
        for (c in sig.chars()) {
            append(
                when (c.toChar()) {
                    'q' -> 'e'
                    'a' -> 'd'
                    'e' -> 'q'
                    'd' -> 'a'
                    else -> c
                }
            )
        }
    }

    class RegisterPatternException(msg: String) : java.lang.Exception(msg)

    /**
     * Maps angle sigs to resource locations so we can look them up in the main registry
     */
    private class Save(val lookup: MutableMap<String, ResourceLocation>) : SavedData() {
        override fun save(tag: CompoundTag): CompoundTag {
            for ((sig, id) in this.lookup) {
                tag.putString(sig, id.toString())
            }
            return tag
        }

        companion object {
            fun load(tag: CompoundTag): Save {
                val map = HashMap<String, ResourceLocation>()
                for (sig in tag.allKeys) {
                    map[sig] = ResourceLocation.tryParse(tag.getString(sig))!!
                }
                return Save(map)
            }

            fun create(seed: Long): Save {
                val map = mutableMapOf<String, ResourceLocation>()
                for ((opId, v) in PatternRegistry.perWorldPatterns) {
                    // waugh why doesn't kotlin recursively destructure things
                    val (pat, _) = v
                    val scrungled = EulerPathFinder.findAltDrawing(pat, seed)
                    map[scrungled.anglesSignature()] = opId
                }
                return Save(map)
            }
        }
    }

    private const val TAG_SAVED_DATA = "hex.per-world-patterns"
}