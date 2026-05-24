package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.DyeColor

class MishapDisallowedSpell(val type: String, val actionKey: ResourceLocation?) : Mishap() {
    @Deprecated("Provide the type (disallowed or disallowed_circle) and the action key that caused the mishap")
    constructor(type: String = "disallowed") : this(type, null) {}

    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.BLACK)

    override fun resolutionType(ctx: CastingEnvironment) = ResolvedPatternType.INVALID

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        // NO-OP
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Component? {
        if (actionKey == null) return error(type + "_generic")
        return error(type, "hexcasting.action.$actionKey".asTranslatedComponent)
    }
}
