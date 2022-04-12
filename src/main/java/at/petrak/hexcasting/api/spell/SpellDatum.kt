package at.petrak.hexcasting.api.spell

import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.utils.HexUtils
import at.petrak.hexcasting.api.utils.HexUtils.serializeToNBT
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidSpellDatumType
import net.minecraft.ChatFormatting
import net.minecraft.nbt.*
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.chat.TranslatableComponent
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
 *  * [Vec3][net.minecraft.world.phys.Vec3] as both position and (when normalized) direction
 *  * [Widget]; [Widget.NULL] is used as our null value
 *  * [List<SpellDatum<*>>][List]
 *  * [HexPattern]! Yes, we have meta-evaluation everyone.
 * The constructor guarantees we won't pass a type that isn't one of those types.
 *
 *
 */
class SpellDatum<T : Any> private constructor(val payload: T) {
    val clazz: Class<T> = payload.javaClass

    fun serializeToNBT(): CompoundTag {
        val out = CompoundTag()
        when (val pl = this.payload) {
            is Entity -> {
                val subtag = CompoundTag()
                subtag.put(TAG_ENTITY_UUID, NbtUtils.createUUID(pl.uuid))
                // waayyghg
                val json = Component.Serializer.toJson(pl.displayName)
                subtag.putString(TAG_ENTITY_NAME_CHEATY, json)
                out.put(TAG_ENTITY, subtag)
            }
            is Double -> out.put(
                TAG_DOUBLE, DoubleTag.valueOf(pl)
            )
            is Vec3 -> out.put(
                TAG_VEC3, pl.serializeToNBT()
            )
            is ArrayList<*> -> {
                val subtag = ListTag()
                for (elt in pl)
                    subtag.add((elt as SpellDatum<*>).serializeToNBT())
                out.put(TAG_LIST, subtag)
            }
            is Widget -> {
                out.putString(TAG_WIDGET, pl.name)
            }
            is HexPattern -> {
                out.put(TAG_PATTERN, pl.serializeToNBT())
            }
            else -> throw RuntimeException("cannot serialize $pl because it is of type ${pl.javaClass.canonicalName} which is not serializable")
        }

        return out
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
        return DisplayFromTag(nbt)
    }

    fun getType(): DatumType =
        when (this.payload) {
            is Entity -> DatumType.ENTITY
            is Widget -> DatumType.WIDGET
            is List<*> -> DatumType.LIST
            is HexPattern -> DatumType.PATTERN
            is Double -> DatumType.DOUBLE
            is Vec3 -> DatumType.VEC
            else -> DatumType.OTHER
        }

    companion object {
        @JvmStatic
        fun make(payload: Any): SpellDatum<*> =
            if (payload is List<*>) {
                SpellDatum(payload.map {
                    when (it) {
                        null -> make(Widget.NULL)
                        is SpellDatum<*> -> it
                        else -> make(it)
                    }
                })
            } else if (payload is Vec3) {
                SpellDatum(
                    Vec3(
                        HexUtils.FixNANs(payload.x),
                        HexUtils.FixNANs(payload.y),
                        HexUtils.FixNANs(payload.z),
                    )
                )
            } else if (IsValidType(payload)) {
                SpellDatum(payload)
            } else if (payload is java.lang.Double) {
                // Check to see if it's a java *boxed* double, for when we call this from Java
                val num = payload.toDouble()
                SpellDatum(HexUtils.FixNANs(num))
            } else {
                throw MishapInvalidSpellDatumType(payload)
            }

        @JvmStatic
        fun DeserializeFromNBT(nbt: CompoundTag, world: ServerLevel): SpellDatum<*> {
            val keys = nbt.allKeys
            if (keys.size != 1)
                throw IllegalArgumentException("Expected exactly one kv pair: $nbt")

            return when (val key = keys.iterator().next()) {
                TAG_ENTITY -> {
                    val subtag = nbt.getCompound(key)
                    val uuid = subtag.getUUID(TAG_ENTITY_UUID) // and throw away name
                    val entity = world.getEntity(uuid)
                    // If the entity died or something return Unit
                    SpellDatum(if (entity == null || !entity.isAlive) Widget.NULL else entity)
                }
                TAG_DOUBLE -> SpellDatum(nbt.getDouble(key))
                TAG_VEC3 -> SpellDatum(HexUtils.DeserializeVec3FromNBT(nbt.getLongArray(key)))
                TAG_LIST -> {
                    val arr = nbt.getList(key, Tag.TAG_COMPOUND.toInt())
                    val out = ArrayList<SpellDatum<*>>(arr.size)
                    for (subtag in arr) {
                        // this is safe because otherwise we wouldn't have been able to get the list before
                        out.add(DeserializeFromNBT(subtag as CompoundTag, world))
                    }
                    SpellDatum(out)
                }
                TAG_WIDGET -> {
                    SpellDatum(Widget.valueOf(nbt.getString(key)))
                }
                TAG_PATTERN -> {
                    SpellDatum(HexPattern.DeserializeFromNBT(nbt.getCompound(TAG_PATTERN)))
                }
                else -> throw IllegalArgumentException("Unknown key $key: $nbt")
            }
        }

        @Deprecated(
            "use the [Level] overload", ReplaceWith(
                "DeserializeFromNBT(nbt, ctx.world)",
                "at.petrak.hexcasting.api.spell.SpellDatum.Companion.DeserializeFromNBT"
            )
        )
        @JvmStatic
        fun DeserializeFromNBT(nbt: CompoundTag, ctx: CastingContext): SpellDatum<*> =
            DeserializeFromNBT(nbt, ctx.world)

        @JvmStatic
        fun DisplayFromTag(nbt: CompoundTag): Component {
            val keys = nbt.allKeys
            if (keys.size != 1)
                throw IllegalArgumentException("Expected exactly one kv pair: $nbt")

            return when (val key = keys.iterator().next()) {
                TAG_DOUBLE -> TextComponent(String.format("%.4f", nbt.getDouble(TAG_DOUBLE))).withStyle(ChatFormatting.GREEN)
                TAG_VEC3 -> {
                    val vec = HexUtils.DeserializeVec3FromNBT(nbt.getLongArray(key))
                    // the focus color is really more red, but we don't want to show an error-y color
                    TextComponent(String.format("(%.2f, %.2f, %.2f)", vec.x, vec.y, vec.z)).withStyle(ChatFormatting.LIGHT_PURPLE)
                }
                TAG_LIST -> {
                    val out = TextComponent("[").withStyle(ChatFormatting.WHITE)

                    val arr = nbt.getList(key, Tag.TAG_COMPOUND.toInt())
                    for ((i, subtag) in arr.withIndex()) {
                        // this is safe because otherwise we wouldn't have been able to get the list before
                        out.append(DisplayFromTag(subtag as CompoundTag))
                        if (i != arr.lastIndex) {
                            out.append(", ")
                        }
                    }

                    out.append("]")
                }
                TAG_WIDGET -> {
                    val widget = Widget.valueOf(nbt.getString(key))
                    if (widget == Widget.GARBAGE) TextComponent("arimfexendrapuse").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.OBFUSCATED)
                    // use dark purple instead of pink, so that vec3 can be pink instead of error red
                    else TextComponent(widget.toString()).withStyle(ChatFormatting.DARK_PURPLE)
                }
                TAG_PATTERN -> {
                    val pat = HexPattern.DeserializeFromNBT(nbt.getCompound(TAG_PATTERN))
                    var angleDesc = pat.anglesSignature()
                    if (angleDesc.isNotBlank()) angleDesc = " $angleDesc";
                    val out = TextComponent("HexPattern(").withStyle(ChatFormatting.GOLD)
                    out.append(TextComponent("${pat.startDir}$angleDesc").withStyle(ChatFormatting.WHITE))
                    out.append(")")
                }
                TAG_ENTITY -> {
                    // handle pre-0.5.0 foci not having the tag
                    try {
                        val subtag = nbt.getCompound(TAG_ENTITY)
                        val json = subtag.getString(TAG_ENTITY_NAME_CHEATY)
                        val out = Component.Serializer.fromJson(json)!!
                        out.withStyle(ChatFormatting.AQUA)
                    } catch (exn: NullPointerException) {
                        TranslatableComponent("hexcasting.spelldata.entity.whoknows").withStyle(ChatFormatting.WHITE)
                    }
                }
                else -> throw IllegalArgumentException("Unknown key $key: $nbt")
            }
        }

        // Set of legal types to go in a spell
        val ValidTypes: Set<Class<*>> = setOf(
            Entity::class.java,
            Double::class.java,
            Vec3::class.java,
            List::class.java,
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

        fun <T : Any> IsValidType(checkee: T): Boolean =
            if (checkee is List<*>) {
                // note it should be impossible to pass a spell datum that doesn't contain a valid type,
                // but we best make sure.
                checkee.all { it is SpellDatum<*> && IsValidType(it.payload) }
            } else {
                ValidTypes.any { clazz -> clazz.isAssignableFrom(checkee.javaClass) }
            }


    }
}
