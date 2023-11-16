package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.DyeColor

class MishapEntityTooFarAway(val entity: Entity) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.PINK)

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        env.mishapEnvironment.yeetHeldItemsTowards(entity.position())
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Component =
        error("entity_too_far", entity.displayName)
}
