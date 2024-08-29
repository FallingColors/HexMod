package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.aqua
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.DyeColor

class MishapEntityNotFound(
    val perpetrator: Iota,
    val reverseIdx: Int,
) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.BROWN)

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        stack[stack.size - 1 - reverseIdx] = GarbageIota();
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) =
        error(
            "entity_not_found", reverseIdx,
            perpetrator.display()
        )
    companion object {
        @JvmStatic
        fun ofType(perpetrator: Iota, reverseIdx: Int): MishapEntityNotFound {
            return of(perpetrator, reverseIdx)
        }

        @JvmStatic
        fun of(perpetrator: Iota, reverseIdx: Int): MishapEntityNotFound {
            val key = "hexcasting.mishap.entity_not_found"
            return MishapEntityNotFound(perpetrator, reverseIdx)
        }
    }
}