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
        val key = IXplatAbstractions.INSTANCE.specialHandlerRegistry.getResourceKey(HexSpecialHandlers.MASK).get()
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
        override fun tryMatch(pat: HexPattern): SpecialHandlerMask? {
            val directions = pat.directions()

            var flatDir = pat.startDir
            if (pat.angles.isNotEmpty() && pat.angles[0] == HexAngle.LEFT_BACK) {
                flatDir = directions[0].rotatedBy(HexAngle.LEFT);
            }

            // TODO: we could probably definitely do this with a long to make it faster
            val mask = BooleanArrayList()
            var i = 0;
            while (i < directions.size) {
                // Angle with respect to the *start direction*
                val angle = directions[i].angleFrom(flatDir);
                if (angle == HexAngle.FORWARD) {
                    mask.add(true)
                    i++
                    continue;
                }
                if (i >= directions.size - 1) {
                    // then we're out of angles!
                    return null
                }
                val angle2 = directions[i + 1].angleFrom(flatDir);
                if (angle == HexAngle.RIGHT && angle2 == HexAngle.LEFT) {
                    mask.add(false)
                    // skip both segments of the dip
                    i += 2
                    continue
                }
                return null
            }
            return SpecialHandlerMask(mask)
        }
    }

    companion object {
        public val NAME: ResourceLocation = modLoc("mask")
    }
}