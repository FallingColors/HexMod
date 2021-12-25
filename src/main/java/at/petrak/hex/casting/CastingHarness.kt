package at.petrak.hex.casting

import at.petrak.hex.HexMod
import at.petrak.hex.HexUtils
import at.petrak.hex.HexUtils.TAU
import at.petrak.hex.HexUtils.deserializeVec3FromNBT
import at.petrak.hex.HexUtils.serializeToNBT
import at.petrak.hex.casting.operators.SpellOperator
import at.petrak.hex.hexes.HexAngle
import at.petrak.hex.hexes.HexCoord
import at.petrak.hex.hexes.HexDir
import at.petrak.hex.hexes.HexPattern
import com.mojang.math.Matrix3f
import com.mojang.math.Vector3f
import net.minecraft.nbt.*
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import java.util.*
import kotlin.math.*

/**
 * Keeps track of a player casting a spell
 */
class CastingHarness private constructor(
    val stack: MutableList<SpellDatum<*>>,
    var patternDrawState: PatternDrawState,
    val worldPoints: MutableList<MutableList<Vec3>>,
    val ctx: CastingContext,
) {
    /**
     * Internally update its own state based on the player's actions.
     */
    fun update(): CastResult {
        val caster = this.ctx.caster
        return when (patternDrawState) {
            is PatternDrawState.BetweenPatterns -> {
                if (caster.isUsingItem) {
                    HexMod.LOGGER.info("Started drawing new pattern")
                    this.patternDrawState = PatternDrawState.JustStarted(caster.lookAngle)
                    worldPoints.add(mutableListOf(caster.lookAngle.add(caster.position())))
                }
                CastResult.Nothing
            }
            is PatternDrawState.JustStarted -> {
                val (anchor) = patternDrawState as PatternDrawState.JustStarted
                if (caster.isUsingItem) {
                    val dir = anchor.hexDirBetween(caster.lookAngle)
                    if (dir.isPresent) {
                        val pat = HexPattern(dir.get())
                        HexMod.LOGGER.info("Started casting spell: $pat")
                        this.patternDrawState = PatternDrawState.Drawing(caster.lookAngle, pat)
                        worldPoints.last().add(caster.lookAngle.add(caster.position()))
                    }
                    CastResult.Nothing
                } else {
                    // We never finished drawing the line
                    this.patternDrawState = PatternDrawState.BetweenPatterns
                    worldPoints.removeLastOrNull()
                    CastResult.Nothing
                }
            }
            is PatternDrawState.Drawing -> {
                val (anchor, pat) = patternDrawState as PatternDrawState.Drawing
                if (caster.isUsingItem) {
                    val dir = anchor.hexDirBetween(caster.lookAngle)
                    if (dir.isPresent && pat.tryAppendDir(dir.get())) {
                        // nice! another line on the pattern
                        HexMod.LOGGER.info("Added dir to pattern: $pat")
                        (patternDrawState as PatternDrawState.Drawing).anchorLookPos = caster.lookAngle
                        worldPoints.last().add(caster.lookAngle.add(caster.position()))
                    }

                    CastResult.Nothing
                } else {
                    // Finish the current pattern!
                    patternDrawState = PatternDrawState.BetweenPatterns
                    try {
                        val operator = SpellOperator.fromPattern(pat)
                        HexMod.LOGGER.info("Executing operator: $operator")
                        // now execute the operator
                        if (operator.argc > this.stack.size)
                            throw CastException(CastException.Reason.NOT_ENOUGH_ARGS, operator.argc, this.stack.size)
                        val args = this.stack.takeLast(operator.argc)
                        // there's gotta be a better way to do this
                        for (_idx in 0 until operator.argc)
                            this.stack.removeLast()
                        val newData = operator.execute(args, this.ctx)
                        this.stack.addAll(newData)

                        HexMod.LOGGER.info("Added new data to stack: ${this.stack}")

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
            }
        }
    }

    fun serializeToNBT(): CompoundTag {
        val out = CompoundTag()

        val stackTag = ListTag()
        for (datum in this.stack)
            stackTag.add(datum.serializeToNBT())
        out.put(TAG_STACK, stackTag)

        out.put(TAG_PDS, this.patternDrawState.serializeToNBT())

        val pointsTag = ListTag()
        for (patblob in this.worldPoints) {
            val subtag = ListTag()
            for (point in patblob) {
                subtag.add(point.serializeToNBT())
            }
            pointsTag.add(subtag)
        }
        out.put(TAG_POINTS, pointsTag)

        return out
    }

    companion object {
        const val TAG_STACK = "stack"
        const val TAG_PDS = "pds"
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

                val pds = PatternDrawState.DeserializeFromNBT(nbt.getCompound(TAG_PDS))

                val pointsTag = nbt.getList(TAG_POINTS, Tag.TAG_LIST.toInt())
                val points = pointsTag.map { patgroup ->
                    (patgroup as ListTag).map { posTag ->
                        val pos = posTag as LongArrayTag
                        deserializeVec3FromNBT(pos.asLongArray)
                    }.toMutableList()
                }.toMutableList()

                CastingHarness(stack, pds, points, ctx)
            } catch (exn: Exception) {
                HexMod.LOGGER.warn("Couldn't load harness from nbt tag, falling back to default: $nbt: $exn")
                CastingHarness(mutableListOf(), PatternDrawState.BetweenPatterns, mutableListOf(), ctx)
            }
        }

        // this is on a unit sphere, where 0 is straight ahead and 1 is straight up (or similar)
        const val HEX_GRID_SPACING = 1.0 / 8.0

        private fun screenToLookMat(look: Vec3): Matrix3f {
            val towardsZenith = Vec3(0.0, 1.0, 0.0).subtract(look).normalize()
            val towardsRight = look.cross(towardsZenith).normalize()

            val ihatPrime = Vector3f(look.add(towardsRight))
            val jhatPrime = Vector3f(look.add(towardsZenith))
            val khatPrime = Vector3f(look)

            val neo = Matrix3f()
            neo.set(0, 0, ihatPrime.x())
            neo.set(0, 1, ihatPrime.y())
            neo.set(0, 2, ihatPrime.z())
            neo.set(1, 0, jhatPrime.x())
            neo.set(1, 1, jhatPrime.y())
            neo.set(1, 2, jhatPrime.z())
            neo.set(2, 0, khatPrime.x())
            neo.set(2, 1, khatPrime.y())
            neo.set(2, 2, khatPrime.z())
            return neo
        }

        private fun Vec3.screenAngle(offset: Vec3): Double {
            // https://gist.github.com/Alwinfy/d6f3e9b22e4432f4446a58ace8812a3c
            // no idea how any of this works
            fun pythag(x: Double, y: Double): Double = sqrt(x * x + y * y)

            val yaw = atan2(this.x, this.z)
            val pitch = atan2(this.y, pythag(this.x, this.z))
            val zeroYaw = offset.yRot(-yaw.toFloat())
            val zeroPitch = zeroYaw.xRot(-pitch.toFloat()).normalize()

            return atan2(asin(zeroPitch.y), asin(-zeroPitch.x))
        }

        private fun Vec3.screenVec(offset: Vec3): Vec2 {
            val angle = this.screenAngle(offset)
            val dy = sin(angle)
            val dx = cos(angle)

            val dist = (this.normalize().subtract(offset.normalize())).length()
            return Vec2((dx * dist).toFloat(), (dy * dist).toFloat())
        }

        /** Check if the two vectors are far enough apart to be more than one hex coord apart */
        private fun Vec3.hexDirBetween(look: Vec3): Optional<HexDir> {
            if (look.x.absoluteValue <= 1e-30 || look.z.absoluteValue <= 1e-30)
                return Optional.empty()

            val dist = (this.normalize().subtract(look.normalize())).length()
            if (dist < HEX_GRID_SPACING)
                return Optional.empty()

            // val angle = this.screenAngle(look)
            val neo = screenToLookMat(this)
            neo.invert()
            val lookScreen = Vector3f(look)
            lookScreen.transform(neo)
            HexMod.LOGGER.info(lookScreen)
            val angle = atan2(lookScreen.y(), lookScreen.x())
            // 0 is right, increases clockwise(?)
            val snappedAngle = angle.div(TAU).mod(6.0).times(6).roundToInt()
            return Optional.of(HexDir.values()[(-snappedAngle + 1).mod(6)])
        }

        /**
         * Taking this as a look vector, get the look vector that is this vector offset
         * by the given coordinate in draw-space.
         */
        private fun Vec3.hexOffsetFrom(offset: HexCoord): Vec3 {
            TODO()
        }
    }

    sealed class PatternDrawState {
        /** We're waiting on the player to right-click again */
        object BetweenPatterns : PatternDrawState()

        /** We just started drawing and haven't drawn the first line yet. */
        data class JustStarted(val anchorLookPos: Vec3) : PatternDrawState()

        /** We've started drawing a pattern for real. */
        data class Drawing(var anchorLookPos: Vec3, val wipPattern: HexPattern) : PatternDrawState()

        fun serializeToNBT(): CompoundTag {
            val (key, value) = when (this) {
                BetweenPatterns -> Pair(TAG_BETWEEN_PATTERNS, ListTag())
                is JustStarted -> {
                    val (anchor) = this
                    Pair(TAG_JUST_STARTED, anchor.serializeToNBT())
                }
                is Drawing -> {
                    val (anchor, pat) = this
                    val subtag = CompoundTag()

                    subtag.put(TAG_ANCHOR, anchor.serializeToNBT())
                    subtag.put(TAG_START_DIR, ByteTag.valueOf(pat.startDir.ordinal.toByte()))
                    val anglesTag = ByteArrayTag(pat.angles.map { it.ordinal.toByte() })
                    subtag.put(TAG_ANGLES, anglesTag)

                    Pair(TAG_DRAWING, subtag)
                }
            }
            val out = CompoundTag()
            out.put(key, value)
            return out
        }

        companion object {
            const val TAG_BETWEEN_PATTERNS = "between_patterns"
            const val TAG_JUST_STARTED = "just_started"
            const val TAG_DRAWING = "drawing"

            const val TAG_ANCHOR = "anchor"
            const val TAG_START_DIR = "start_dir"
            const val TAG_ANGLES = "angles"

            fun DeserializeFromNBT(nbt: CompoundTag): PatternDrawState {
                val keys = nbt.allKeys
                if (keys.size != 1)
                    throw IllegalArgumentException("Expected exactly one kv pair: $nbt")

                return when (val key = keys.iterator().next()) {
                    TAG_BETWEEN_PATTERNS -> BetweenPatterns
                    TAG_JUST_STARTED -> {
                        val anchor = HexUtils.deserializeVec3FromNBT(nbt.getLongArray(key))
                        JustStarted(anchor)
                    }
                    TAG_DRAWING -> {
                        val subtag = nbt.getCompound(key)
                        val anchor = HexUtils.deserializeVec3FromNBT(subtag.getLongArray(TAG_ANCHOR))
                        val startDir = HexDir.values()[subtag.getByte(TAG_START_DIR).toInt()]
                        val angles = subtag.getByteArray(TAG_ANGLES).map { HexAngle.values()[it.toInt()] }

                        Drawing(anchor, HexPattern(startDir, angles.toMutableList()))
                    }
                    else -> throw IllegalArgumentException("Unknown key $key: $nbt")
                }
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