package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.Vector
import at.petrak.hexcasting.api.utils.aqua
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.DyeColor

class MishapBadEntity(val entity: Entity, val wanted: Component) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.BROWN)

    override fun executeReturnStack(env: CastingEnvironment, errorCtx: Context, stack: Vector<Iota>): Vector<Iota> {
        env.mishapEnvironment.yeetHeldItemsTowards(entity.position())
        return stack
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) =
        error("bad_entity", wanted, entity.displayName.plainCopy().aqua)

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
