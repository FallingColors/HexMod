package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.network.chat.Component

/**
 * Probably should never be thrown, but happens if a spell circle's flow is messed up during execution somehow.
 */
class MishapMessedUpSpellCircle : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer {
        TODO("Not yet implemented")
    }

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        TODO("Not yet implemented")
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component {
        TODO("Not yet implemented")
    }

}