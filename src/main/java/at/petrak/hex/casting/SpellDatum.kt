package at.petrak.hex.casting

import at.petrak.hex.HexUtils
import at.petrak.hex.HexUtils.serializeToNBT
import net.minecraft.nbt.*
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

/**
 * Data allowed into a spell.
 *
 * We use the following types:
 *  * [Entity]
 *  * [Double]
 *  * [Vec3][net.minecraft.world.phys.Vec3] as both position and (when normalized) direction
 *  * [RenderedSpell]
 *  * [Unit] as our type-safe null
 *  * [ArrayList<SpellDatum<*>>][ArrayList]
 * The constructor guarantees we won't pass a type that isn't one of those types.
 *
 * Please do not access the `payload` field directly (use [tryGet])
 */
class SpellDatum<T : Any> private constructor(val payload: T) {
    val clazz: Class<T> = payload.javaClass

    inline fun <reified U> tryGet(): U =
        if (payload is U) {
            // learning from psi's mistakes
            if (payload is Double && !payload.isFinite())
                0.0 as U
            else
                payload
        } else {
            throw CastException(CastException.Reason.OP_WRONG_TYPE, U::class.java, this.payload)
        }

    fun serializeToNBT(): CompoundTag {
        val out = CompoundTag()
        when (val pl = this.payload) {
            is Entity -> out.put(
                TAG_ENTITY, NbtUtils.createUUID(pl.uuid)
            )
            is Double -> out.put(
                TAG_DOUBLE, DoubleTag.valueOf(pl)
            )
            is Vec3 -> out.put(
                TAG_VEC3, pl.serializeToNBT()
            )
            // use an empty list as unit? works for me i guess
            // it doesn't really matter what we put
            is Unit -> out.put(TAG_UNIT, ListTag())

            is ArrayList<*> -> {
                val subtag = ListTag()
                for (elt in pl)
                    subtag.add((elt as SpellDatum<*>).serializeToNBT())
                out.put(TAG_LIST, subtag)
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

    companion object {
        fun <T : Any> make(payload: T): SpellDatum<T> =
            if (!IsValidType(payload)) {
                throw CastException(CastException.Reason.INVALID_TYPE, payload)
            } else {
                SpellDatum(payload)
            }

        fun DeserializeFromNBT(nbt: CompoundTag, ctx: CastingContext): SpellDatum<*> {
            val keys = nbt.allKeys
            if (keys.size != 1)
                throw IllegalArgumentException("Expected exactly one kv pair: $nbt")

            return when (val key = keys.iterator().next()) {
                TAG_ENTITY -> {
                    val uuid = nbt.getUUID(key)
                    val entity = ctx.world.getEntity(uuid)
                    // If the entity died or something return Unit
                    SpellDatum(if (entity == null || !entity.isAlive) Unit else entity)
                }
                TAG_DOUBLE -> SpellDatum(nbt.getDouble(key))
                TAG_VEC3 -> SpellDatum(HexUtils.deserializeVec3FromNBT(nbt.getLongArray(key)))
                TAG_UNIT -> SpellDatum(Unit)
                TAG_LIST -> {
                    val arr = nbt.getList(key, Tag.TAG_COMPOUND.toInt())
                    val out = ArrayList<SpellDatum<*>>(arr.size)
                    for (subtag in arr) {
                        // this is safe because otherwise we wouldn't have been able to get the list before
                        out.add(DeserializeFromNBT(subtag as CompoundTag, ctx))
                    }
                    SpellDatum(out)
                }
                else -> throw IllegalArgumentException("Unknown key $key: $nbt")
            }
        }

        // Maps the class to the tag name
        val ValidTypes: Set<Class<*>> = setOf(
            Entity::class.java,
            Double::class.java,
            Vec3::class.java,
            RenderedSpell::class.java,
            Unit::class.java,
            ArrayList::class.java,
        )
        const val TAG_ENTITY = "entity"
        const val TAG_DOUBLE = "double"
        const val TAG_VEC3 = "vec3"
        const val TAG_UNIT = "unit"
        const val TAG_LIST = "list"

        fun <T : Any> IsValidType(checkee: T): Boolean =
            if (checkee is ArrayList<*>) {
                checkee.all { it is SpellDatum<*> && IsValidType(it) }
            } else {
                ValidTypes.any { clazz -> clazz.isAssignableFrom(checkee.javaClass) }
            }
    }
}
