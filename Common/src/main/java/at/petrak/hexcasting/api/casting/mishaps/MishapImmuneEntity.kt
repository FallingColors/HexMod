package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.utils.aqua
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.DyeColor

class MishapImmuneEntity(val entity: Entity) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.BLUE)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        yeetHeldItemsTowards(ctx, entity.position())
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) =
        error("immune_entity", entity.displayName.plainCopy().aqua)
}
