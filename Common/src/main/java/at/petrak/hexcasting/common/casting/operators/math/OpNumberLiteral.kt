package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.casting.Action
import at.petrak.hexcasting.api.casting.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import at.petrak.hexcasting.api.utils.lightPurple
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class OpNumberLiteral(val x: Double) : ConstMediaAction {
    override val argc: Int = 0

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        return this.x.asActionResult
    }

    override fun getDisplayName(resLoc: ResourceLocation): Component {
        return "hexcasting.spell.${resLoc.toString()}".asTranslatedComponent(Action.DOUBLE_FORMATTER.format(x)).lightPurple
    }
}