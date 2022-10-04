package at.petrak.hexcasting.api

import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.math.EulerPathFinder
import at.petrak.hexcasting.api.spell.math.HexDir
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidPattern
import at.petrak.hexcasting.api.utils.getSafe
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
    private val operatorLookup = ConcurrentHashMap<ResourceLocation, Operator>()
    private val keyLookup = ConcurrentHashMap<Operator, ResourceLocation>()
    private val specialHandlers: ConcurrentLinkedDeque<SpecialHandlerEntry> = ConcurrentLinkedDeque()

    // Map signatures to the "preferred" direction they start in and their operator ID.
    private val regularPatternLookup: ConcurrentMap<String, RegularEntry> =
        ConcurrentHashMap()

    private val perWorldPatternLookup: ConcurrentMap<ResourceLocation, PerWorldEntry> =
        ConcurrentHashMap()

    /**
     * Associate a given angle signature with a SpellOperator.
     */
    @JvmStatic
    @JvmOverloads
    @Throws(RegisterPatternException::class)
    fun mapPattern(pattern: HexPattern, id: ResourceLocation, operator: Operator, isPerWorld: Boolean = false) {
        this.operatorLookup[id]?.let {
            throw RegisterPatternException("The operator with id `$id` was already registered to: $it")
        }

        this.operatorLookup[id] = operator
        this.keyLookup[operator] = id
        if (isPerWorld) {
            this.perWorldPatternLookup[id] = PerWorldEntry(pattern, id)
        } else {
            this.regularPatternLookup[pattern.anglesSignature()] = RegularEntry(pattern.startDir, id)
        }

    }


    /**
     * Add a special handler, to take an arbitrary pattern and return whatever kind of operator you like.
     */
    @JvmStatic
    fun addSpecialHandler(handler: SpecialHandlerEntry) {
        this.specialHandlers.add(handler)
    }

    @JvmStatic
    fun addSpecialHandler(id: ResourceLocation, handler: SpecialHandler) {
        this.addSpecialHandler(SpecialHandlerEntry(id, handler))
    }

    /**
     * Internal use only.
     */
    @JvmStatic
    fun matchPattern(pat: HexPattern, overworld: ServerLevel): Operator =
        matchPatternAndID(pat, overworld).first

    /**
     * Internal use only.
     */
    @JvmStatic
    fun matchPatternAndID(pat: HexPattern, overworld: ServerLevel): Pair<Operator, ResourceLocation> {
        // Is it global?
        val sig = pat.anglesSignature()
        this.regularPatternLookup[sig]?.let {
            val op = this.operatorLookup[it.opId] ?: throw MishapInvalidPattern()
            return op to it.opId
        }

        // Look it up in the world?
        val ds = overworld.dataStorage
        val perWorldPatterns: Save =
            ds.computeIfAbsent(Save.Companion::load, { Save.create(overworld.seed) }, TAG_SAVED_DATA)
        perWorldPatterns.fillMissingEntries(overworld.seed)
        perWorldPatterns.lookup[sig]?.let {
            val op = this.operatorLookup[it.first]!!
            return op to it.first
        }

        // Lookup a special handler
        // Do this last to prevent conflicts with great spells; this has happened a few times with
        // create phial hahaha
        for (handler in specialHandlers) {
            val op = handler.handler.handlePattern(pat)
            if (op != null) return op to handler.id
        }

        throw MishapInvalidPattern()
    }

    /**
     * Internal use only.
     */
    @JvmStatic
    fun getPerWorldPatterns(overworld: ServerLevel): Map<String, Pair<ResourceLocation, HexDir>> {
        val ds = overworld.dataStorage
        val perWorldPatterns: Save =
            ds.computeIfAbsent(Save.Companion::load, { Save.create(overworld.seed) }, TAG_SAVED_DATA)
        return perWorldPatterns.lookup
    }

    /**
     * Internal use only.
     */
    @JvmStatic
    fun lookupPattern(op: Operator): ResourceLocation? = this.keyLookup[op]

    /**
     * Internal use only.
     */
    @JvmStatic
    fun lookupPattern(opId: ResourceLocation): PatternEntry {
        this.perWorldPatternLookup[opId]?.let {
            return PatternEntry(it.prototype, this.operatorLookup[it.opId]!!, true)
        }
        for ((sig, entry) in this.regularPatternLookup) {
            if (entry.opId == opId) {
                val pattern = HexPattern.fromAngles(sig, entry.preferredStart)
                return PatternEntry(pattern, this.operatorLookup[entry.opId]!!, false)
            }
        }

        throw IllegalArgumentException("could not find a pattern for $opId")
    }

    /**
     * Internal use only.
     */
    @JvmStatic
    fun getAllPerWorldPatternNames(): Set<ResourceLocation> {
        return this.perWorldPatternLookup.keys.toSet()
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

    data class SpecialHandlerEntry(val id: ResourceLocation, val handler: SpecialHandler)

    class RegisterPatternException(msg: String) : Exception(msg)

    private data class RegularEntry(val preferredStart: HexDir, val opId: ResourceLocation)
    private data class PerWorldEntry(val prototype: HexPattern, val opId: ResourceLocation)

    // Fake class we pretend to use internally
    data class PatternEntry(val prototype: HexPattern, val operator: Operator, val isPerWorld: Boolean)

    /**
     * Maps angle sigs to resource locations and their preferred start dir so we can look them up in the main registry
     */

    class Save(val lookup: MutableMap<String, Pair<ResourceLocation, HexDir>>, var missingEntries: Boolean) : SavedData() {
        constructor(lookup: MutableMap<String, Pair<ResourceLocation, HexDir>>) : this(lookup, missingAny(lookup))

        override fun save(tag: CompoundTag): CompoundTag {
            for ((sig, rhs) in this.lookup) {
                val (id, startDir) = rhs
                val entry = CompoundTag()
                entry.putString(TAG_OP_ID, id.toString())
                entry.putByte(TAG_START_DIR, startDir.ordinal.toByte())
                tag.put(sig, entry)
            }
            return tag
        }

        fun fillMissingEntries(seed: Long) {
            if (missingEntries) {
                var doneAny = false

                val allIds = lookup.values.map { it.first }
                for ((prototype, opId) in perWorldPatternLookup.values) {
                    if (opId !in allIds) {
                        scrungle(lookup, prototype, opId, seed)
                        doneAny = true
                    }
                }

                if (doneAny) {
                    setDirty()
                    missingEntries = false
                }
            }
        }

        companion object {
            fun missingAny(lookup: MutableMap<String, Pair<ResourceLocation, HexDir>>): Boolean {
                val allIds = lookup.values.map { it.first }
                return perWorldPatternLookup.values.any { it.opId !in allIds }
            }

            fun load(tag: CompoundTag): Save {
                val map = HashMap<String, Pair<ResourceLocation, HexDir>>()
                val allIds = mutableSetOf<ResourceLocation>()
                for (sig in tag.allKeys) {
                    val entry = tag.getCompound(sig)
                    val opId = ResourceLocation.tryParse(entry.getString(TAG_OP_ID)) ?: continue
                    allIds.add(opId)
                    val startDir = HexDir.values().getSafe(entry.getByte(TAG_START_DIR))
                    map[sig] = opId to startDir
                }
                val missingEntries = perWorldPatternLookup.values.any { it.opId !in allIds }
                return Save(map, missingEntries)
            }

            fun scrungle(lookup: MutableMap<String, Pair<ResourceLocation, HexDir>>, prototype: HexPattern, opId: ResourceLocation, seed: Long) {
                val scrungled = EulerPathFinder.findAltDrawing(prototype, seed) {
                    val sig = it.anglesSignature()
                    !lookup.contains(sig) &&
                            !regularPatternLookup.contains(sig)
                            && specialHandlers.none { handler -> handler.handler.handlePattern(it) != null }
                }
                lookup[scrungled.anglesSignature()] = opId to scrungled.startDir
            }

            @JvmStatic
            fun create(seed: Long): Save {
                val map = mutableMapOf<String, Pair<ResourceLocation, HexDir>>()
                for ((prototype, opId) in perWorldPatternLookup.values) {
                    scrungle(map, prototype, opId, seed)
                }
                val save = Save(map)
                save.setDirty()
                return save
            }
        }
    }

    const val TAG_SAVED_DATA = "hex.per-world-patterns"
    private const val TAG_OP_ID = "op_id"
    private const val TAG_START_DIR = "start_dir"

    @JvmStatic
    fun getPatternCountInfo(): String = "Loaded ${regularPatternLookup.size} regular patterns, " +
            "${perWorldPatternLookup.size} per-world patterns, and " +
            "${specialHandlers.size} special handlers."
}
