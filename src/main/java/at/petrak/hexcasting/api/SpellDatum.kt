package at.petrak.hexcasting.api

import at.petrak.hexcasting.HexUtils
import at.petrak.hexcasting.HexUtils.serializeToNBT
import at.petrak.hexcasting.common.casting.CastException
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.Widget
import at.petrak.hexcasting.hexmath.HexPattern
import net.minecraft.ChatFormatting
import net.minecraft.nbt.*
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

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

    inline fun <reified U> tryGet(): U =
        if (payload is U) {
            payload
        } else {
            throw CastException(CastException.Reason.OP_WRONG_TYPE, U::class.java, this.payload)
        }

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

    fun display(): Component {
        val nbt = this.serializeToNBT()
        return DisplayFromTag(nbt)
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
                throw CastException(CastException.Reason.INVALID_TYPE, payload)
            }


        @JvmStatic
        fun DeserializeFromNBT(nbt: CompoundTag, ctx: CastingContext): SpellDatum<*> {
            val keys = nbt.allKeys
            if (keys.size != 1)
                throw IllegalArgumentException("Expected exactly one kv pair: $nbt")

            return when (val key = keys.iterator().next()) {
                TAG_ENTITY -> {
                    val subtag = nbt.getCompound(key)
                    val uuid = subtag.getUUID(TAG_ENTITY_UUID) // and throw away name
                    val entity = ctx.world.getEntity(uuid)
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
                        out.add(DeserializeFromNBT(subtag as CompoundTag, ctx))
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

        @JvmStatic
        fun DisplayFromTag(nbt: CompoundTag): Component {
            val keys = nbt.allKeys
            if (keys.size != 1)
                throw IllegalArgumentException("Expected exactly one kv pair: $nbt")

            return when (val key = keys.iterator().next()) {
                TAG_DOUBLE -> TextComponent(String.format("§a%.4f§r", nbt.getDouble(TAG_DOUBLE)))
                TAG_VEC3 -> {
                    val vec = HexUtils.DeserializeVec3FromNBT(nbt.getLongArray(key))
                    // the focus color is really more red, but we don't want to show an error-y color
                    TextComponent(String.format("§d(%.2f, %.2f, %.2f)§r", vec.x, vec.y, vec.z))
                }
                TAG_LIST -> {
                    val out = TextComponent("[")

                    val arr = nbt.getList(key, Tag.TAG_COMPOUND.toInt())
                    for ((i, subtag) in arr.withIndex()) {
                        // this is safe because otherwise we wouldn't have been able to get the list before
                        out.append(DisplayFromTag(subtag as CompoundTag))
                        if (i != arr.lastIndex) {
                            out.append(", ")
                        }
                    }

                    out.append("]")
                    out
                }
                TAG_WIDGET -> {
                    val widget = Widget.valueOf(nbt.getString(key))
                    TextComponent(
                        if (widget == Widget.GARBAGE) {
                            "§8§karimfexendrapuse§r"
                        } else {
                            // use dark purple instead of pink, so that vec3 can be pink instead of error red
                            "§5$widget§r"
                        }
                    )
                }
                TAG_PATTERN -> {
                    val pat = HexPattern.DeserializeFromNBT(nbt.getCompound(TAG_PATTERN))
                    val out = TextComponent("§6HexPattern")
                    out.append(pat.startDir.toString())
                    out.append(" ")
                    out.append(pat.anglesSignature())
                    out.append(")§r")
                    out
                }
                TAG_ENTITY -> {
                    // handle pre-0.5.0 foci not having the tag
                    try {
                        val subtag = nbt.getCompound(TAG_ENTITY)
                        val json = subtag.getString(TAG_ENTITY_NAME_CHEATY)
                        val out = Component.Serializer.fromJson(json)!!
                        out.style = Style.EMPTY.withColor(ChatFormatting.AQUA)
                        out
                    } catch (exn: NullPointerException) {
                        TranslatableComponent("hexcasting.spelldata.entity.whoknows")
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
