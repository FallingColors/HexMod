package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

class MishapNotEnoughMedia(private val cost: Long) : Mishap() {
    override fun accentColor(env: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.RED)

    override fun resolutionType(env: CastingEnvironment) = ResolvedPatternType.ERRORED

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: TreeList<Iota>): TreeList<Iota> {
        env.extractMedia(cost, false)
        return stack
    }

    override fun errorMessage(env: CastingEnvironment, errorCtx: Context): Component {
        if (env is CircleCastEnv) {
            return "hexcasting.mishap.circle_lacks_media".asTranslatedComponent
        } else {
            return "hexcasting.message.cant_overcast".asTranslatedComponent
        }
    }
}