package at.petrak.hexcasting.common.casting.operators.stack

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import at.petrak.hexcasting.api.utils.lightPurple
import it.unimi.dsi.fastutil.booleans.BooleanList
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class OpMask(val mask: BooleanList, val key: ResourceLocation) : ConstMediaAction {
    override val argc: Int
        get() = mask.size

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val out = ArrayList<Iota>(this.mask.size)
        for ((i, include) in this.mask.withIndex()) {
            if (include)
                out.add(args[i])
        }
        return out
    }

    override val displayName: Component
        get() = "hexcasting.spell.$key".asTranslatedComponent(mask.map { if (it) '-' else 'v' }.joinToString("")).lightPurple
}
