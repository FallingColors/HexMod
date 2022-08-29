package at.petrak.hexcasting.api.spell

import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidSpellDatumType
import at.petrak.hexcasting.api.utils.*
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import java.util.*

/**
 * Data allowed into a spell.
 *
 * We use the following types:
 *  * [Entity]
 *  * [Double]
 *  * [Vec3] as both position and (when normalized) direction
 *  * [Widget]; [Widget.NULL] is used as our null value
 *  * [SpellList]
 *  * [HexPattern]! Yes, we have meta-evaluation everyone.
 * The constructor guarantees we won't pass a type that isn't one of those types.
 *
 *
 */
class SpellDatum<T : Any> private constructor(val payload: T) {
    val clazz: Class<T> = payload.javaClass

    fun serializeToNBT(): CompoundTag = this.serializeToNBTWithDepthCheck(0, 0)?.first
        ?: NBTBuilder { TAG_WIDGET %= Widget.GARBAGE.name }


    // The second int returned is the number of datums contained in this one.
    fun serializeToNBTWithDepthCheck(depth: Int, total: Int): Pair<CompoundTag, Int>? {
        if (total > MAX_SERIALIZATION_TOTAL)
            return null

        val tag = NBTBuilder {
            when (val pl = payload) {
                is SpellList -> {
                    // handle it specially!
                    if (depth + 1 > MAX_SERIALIZATION_DEPTH) {
                        return null
                    }

                    val outList = ListTag()
                    var total1 = total + 1 // make mutable and include the list itself
                    for (elt in pl) {
                        val (t, subtotal) = elt.serializeToNBTWithDepthCheck(depth + 1, total1) ?: return null
                        total1 = subtotal
                        outList.add(t)
                    }
                    return Pair(
                        NBTBuilder { TAG_LIST %= outList },
                        total1
                    )
                }
                is Entity -> TAG_ENTITY %= compound {
                    TAG_ENTITY_UUID %= NbtUtils.createUUID(pl.uuid)
                    TAG_ENTITY_NAME_CHEATY %= Component.Serializer.toJson(pl.displayName)
                }
                is Double -> TAG_DOUBLE %= pl
                is Vec3 -> TAG_VEC3 %= pl.serializeToNBT()
                is Widget -> TAG_WIDGET %= pl.name
                is HexPattern -> TAG_PATTERN %= pl.serializeToNBT()
                else -> throw RuntimeException("cannot serialize $pl because it is of type ${pl.javaClass.canonicalName} which is not serializable")
            }
        }
        return Pair(tag, total + 1)
    }

    override fun toString(): String =
        buildString {
            append("SpellDatum[")
            append(this@SpellDatum.payload.toString())
            append(']')
        }

    override fun equals(other: Any?): Boolean {
        return other is SpellDatum<*> && other.payload == payload
    }

    override fun hashCode(): Int {
        return Objects.hash(clazz, this.payload)
    }

    fun display(): Component {
        val nbt = this.serializeToNBT()
        return displayFromNBT(nbt)
    }

    fun getType(): DatumType =
        when (this.payload) {
            is Entity -> DatumType.ENTITY
            is Widget -> DatumType.WIDGET
            is SpellList -> DatumType.LIST
            is HexPattern -> DatumType.PATTERN
            is Double -> DatumType.DOUBLE
            is Vec3 -> DatumType.VEC
            else -> DatumType.OTHER
        }

    companion object {
        const val MAX_SERIALIZATION_DEPTH = 256
        const val MAX_SERIALIZATION_TOTAL = 1024

        @JvmStatic
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        fun make(payload: Any): SpellDatum<*> =
            if (payload is SpellDatum<*>) {
                payload
            } else if (payload is List<*>) {
                SpellDatum(SpellList.LList(0, payload.map {
                    when (it) {
                        null -> make(Widget.NULL)
                        else -> make(it)
                    }
                }))
            } else if (payload is Vec3) {
                SpellDatum(
                    Vec3(
                        fixNAN(payload.x),
                        fixNAN(payload.y),
                        fixNAN(payload.z),
                    )
                )
            } else if (isValidType(payload)) {
                SpellDatum(payload)
            } else if (payload is java.lang.Double) {
                // Check to see if it's a java *boxed* double, for when we call this from Java
                val num = payload.toDouble()
                SpellDatum(fixNAN(num))
            } else {
                throw MishapInvalidSpellDatumType(payload)
            }

        @JvmStatic
        fun fromNBT(nbt: CompoundTag, world: ServerLevel): SpellDatum<*> {
            val keys = nbt.allKeys
            if (keys.size != 1)
                return SpellDatum(Widget.GARBAGE) // Invalid iota format

            return when (val key = keys.iterator().next()) {
                TAG_ENTITY -> {
                    val subtag = nbt.getCompound(key)
                    val uuid = subtag.getUUID(TAG_ENTITY_UUID) // and throw away name
                    val entity = world.getEntity(uuid)
                    // If the entity died or something return Unit
                    SpellDatum(if (entity == null || !entity.isAlive) Widget.NULL else entity)
                }
                TAG_DOUBLE -> SpellDatum(nbt.getDouble(key))
                TAG_VEC3 -> SpellDatum(vecFromNBT(nbt.getLongArray(key)))
                TAG_LIST -> {
                    SpellDatum(SpellList.fromNBT(nbt.getList(key, Tag.TAG_COMPOUND), world))
                }
                TAG_WIDGET -> {
                    SpellDatum(Widget.fromString(nbt.getString(key)))
                }
                TAG_PATTERN -> {
                    SpellDatum(HexPattern.fromNBT(nbt.getCompound(TAG_PATTERN)))
                }
                else -> SpellDatum(Widget.GARBAGE) // Invalid iota type
            }
        }

        @Deprecated(
            "use the [Level] overload", ReplaceWith(
                "DeserializeFromNBT(nbt, ctx.world)",
                "at.petrak.hexcasting.api.spell.SpellDatum.Companion.DeserializeFromNBT"
            )
        )
        @JvmStatic
        fun fromNBT(nbt: CompoundTag, ctx: CastingContext): SpellDatum<*> =
            fromNBT(nbt, ctx.world)

        @JvmStatic
        fun displayFromNBT(nbt: CompoundTag): Component {
            val keys = nbt.allKeys
            val out = "".asTextComponent

            if (keys.size != 1)
                out += "hexcasting.spelldata.unknown".asTranslatedComponent.white
            else {
                when (val key = keys.iterator().next()) {
                    TAG_DOUBLE -> out += String.format(
                        "%.4f",
                        nbt.getDouble(TAG_DOUBLE)
                    ).green
                    TAG_VEC3 -> {
                        val vec = vecFromNBT(nbt.getLongArray(key))
                        // the focus color is really more red, but we don't want to show an error-y color
                        out += String.format(
                            "(%.2f, %.2f, %.2f)",
                            vec.x,
                            vec.y,
                            vec.z
                        ).lightPurple
                    }
                    TAG_LIST -> {
                        out += "[".white

                        val arr = nbt.getList(key, Tag.TAG_COMPOUND)
                        for ((i, subtag) in arr.withIndex()) {
                            out += displayFromNBT(subtag.asCompound)
                            if (i != arr.lastIndex) {
                                out += ", ".white
                            }
                        }

                        out += "]".white
                    }
                    TAG_WIDGET -> {
                        val widget = Widget.fromString(nbt.getString(key))

                        out += if (widget == Widget.GARBAGE)
                            "arimfexendrapuse".darkGray.obfuscated
                        else
                            widget.toString().darkPurple
                    }
                    TAG_PATTERN -> {
                        val pat = HexPattern.fromNBT(nbt.getCompound(TAG_PATTERN))
                        var angleDesc = pat.anglesSignature()
                        if (angleDesc.isNotBlank()) angleDesc = " $angleDesc"
                        out += "HexPattern(".gold
                        out += "${pat.startDir}$angleDesc".white
                        out += ")".gold
                    }
                    TAG_ENTITY -> {
                        val subtag = nbt.getCompound(TAG_ENTITY)
                        val json = subtag.getString(TAG_ENTITY_NAME_CHEATY)
                        // handle pre-0.5.0 foci not having the tag
                        out += Component.Serializer.fromJson(json)?.aqua
                            ?: "hexcasting.spelldata.entity.whoknows".asTranslatedComponent.white
                    }
                    else -> {
                        out += "hexcasting.spelldata.unknown".asTranslatedComponent.white
                    }
                }
            }
            return out
        }

        // Set of legal types to go in a spell
        val ValidTypes: Set<Class<*>> = setOf(
            Entity::class.java,
            Double::class.java,
            Vec3::class.java,
            SpellList::class.java,
            Widget::class.java,
            HexPattern::class.java,
        )

        const val TAG_ENTITY = "entity"
        const val TAG_DOUBLE = "double"
        const val TAG_VEC3 = "vec3"
        const val TAG_LIST = "list"
        const val TAG_WIDGET = "widget"
        const val TAG_PATTERN = "pattern"

        const val TAG_ENTITY_UUID = "uuid"

        // Also encode the entity's name as a component for the benefit of the client
        const val TAG_ENTITY_NAME_CHEATY = "name"

        fun <T : Any> isValidType(checkee: T): Boolean =
            ValidTypes.any { clazz -> clazz.isAssignableFrom(checkee.javaClass) }

        @JvmStatic
        fun tagForType(datumType: DatumType): String {
            return when (datumType) {
                DatumType.ENTITY -> TAG_ENTITY
                DatumType.WIDGET -> TAG_WIDGET
                DatumType.LIST -> TAG_LIST
                DatumType.PATTERN -> TAG_PATTERN
                DatumType.DOUBLE -> TAG_DOUBLE
                DatumType.VEC -> TAG_VEC3
                DatumType.OTHER, DatumType.EMPTY -> ""
            }
        }


    }
}
