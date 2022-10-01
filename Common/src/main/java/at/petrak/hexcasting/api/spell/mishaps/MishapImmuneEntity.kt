package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.utils.aqua
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.DyeColor

class MishapImmuneEntity(val entity: Entity) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.BLUE)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        yeetHeldItemsTowards(ctx, entity.position())
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) =
        error("immune_entity", actionName(errorCtx.action), entity.displayName.plainCopy().aqua)
}
