package at.petrak.hexcasting.common.casting.actions.stack

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.HexAPI.modLoc
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.castables.SpecialHandler
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.math.HexAngle
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import at.petrak.hexcasting.api.utils.lightPurple
import at.petrak.hexcasting.common.lib.hex.HexSpecialHandlers
import at.petrak.hexcasting.xplat.IXplatAbstractions
import it.unimi.dsi.fastutil.booleans.BooleanArrayList
import it.unimi.dsi.fastutil.booleans.BooleanList
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class SpecialHandlerMask(val mask: BooleanList) : SpecialHandler {
    override fun act(): Action {
        return InnerAction(this.mask)
    }

    override fun getName(): Component {
        val key = IXplatAbstractions.INSTANCE.specialHandlerRegistry.getResourceKey(HexSpecialHandlers.MASK.get()).get()
        val fingerprint = mask.map { if (it) '-' else 'v' }.joinToString("")
        return HexAPI.instance().getSpecialHandlerI18nKey(key)
            .asTranslatedComponent(fingerprint)
            .lightPurple
    }

    class InnerAction(val mask: BooleanList) : ConstMediaAction {
        override val argc: Int
            get() = this.mask.size

        override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
            val out = ArrayList<Iota>(this.mask.size)
            for ((i, include) in this.mask.withIndex()) {
                if (include)
                    out.add(args[i])
            }
            return out
        }
    }

    class Factory : SpecialHandler.Factory<SpecialHandlerMask> {
        override fun tryMatch(pat: HexPattern, env: CastingEnvironment): SpecialHandlerMask? {
            val iterator = pat.signature.iterator()
            // if there's a first angle we need it
            val firstTurn = if (iterator.hasNext()) iterator.next() else null

            // if we start with `a`, that means the first segment is a spike
            // this means if we extended backwards, the spike would start with `e`
            // meaning the direction of flatness is the inverse of `e`, which is `q`
            //
            // if we don't start with `a`, we started with something flat
            val flatDir = if (firstTurn == HexAngle.LEFT_BACK) {
                pat.orientation.rotatedBy(HexAngle.LEFT)
            } else {
                pat.orientation
            }

            val mask = BooleanArrayList()
            var direction = pat.orientation

            var currentAngleFromForward = direction.angleFrom(flatDir)
            // 0 angles = 1 straight line, so we have to hold the angle between our current segment and next segment
            var nextAngle = firstTurn

            while (true) {
                if (currentAngleFromForward == HexAngle.FORWARD) {
                    // a line means keep this stack element
                    mask.add(true)

                    // no more angles, we're done
                    if (nextAngle == null) break

                    // since this was a flat segment, we just advance to the next segment
                    direction *= nextAngle
                    currentAngleFromForward = direction.angleFrom(flatDir)
                    nextAngle = if (iterator.hasNext()) iterator.next() else null
                } else if (currentAngleFromForward == HexAngle.RIGHT) {
                    // if we are starting a spike, but there's nothing after the start, we don't match
                    if (nextAngle == null) return null

                    // if we aren't finishing the spike with `a` (`q` relative to flatDir), we don't match
                    direction *= nextAngle
                    if (direction.angleFrom(flatDir) != HexAngle.LEFT) return null

                    // a spike means drop this stack element
                    mask.add(false)

                    // chomp the end of the spike
                    nextAngle = if (iterator.hasNext()) iterator.next() else null
                    // if there's nothing after the spike, we're done
                    if (nextAngle == null) break

                    // since we chomped the end of the spike, we just advance to the next segment
                    direction *= nextAngle
                    currentAngleFromForward = direction.angleFrom(flatDir)
                    nextAngle = if (iterator.hasNext()) iterator.next() else null
                } else {
                    // only FORWARD and RIGHT can appear as transitions in the state machine
                    return null
                }
            }

            return SpecialHandlerMask(mask)
        }
    }

    companion object {
        public val NAME: ResourceLocation = modLoc("mask")
    }
}