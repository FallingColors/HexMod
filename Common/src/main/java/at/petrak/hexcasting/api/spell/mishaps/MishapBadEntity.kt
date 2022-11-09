package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.utils.aqua
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.DyeColor

class MishapBadEntity(val entity: Entity, val wanted: Component) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.BROWN)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        yeetHeldItemsTowards(ctx, entity.position())
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) =
        error("bad_entity", actionName(errorCtx.action), wanted, entity.displayName.plainCopy().aqua)

    companion object {
        @JvmStatic
        fun of(entity: Entity, stub: String): Mishap {
            val component = "hexcasting.mishap.bad_item.$stub".asTranslatedComponent
            if (entity is ItemEntity)
                return MishapBadItem(entity, component)
            return MishapBadEntity(entity, component)
        }
    }
}
