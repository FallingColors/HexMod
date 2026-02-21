package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.aqua
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor
import java.util.*

class MishapEntityNotFound(val entityId: UUID, val entityName: Component?) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.BROWN)

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        env.mishapEnvironment.nauseate(3 * 20)
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) =
        error("entity_not_found", entityName?.plainCopy()?.aqua ?: Component.literal(entityId.toString()).withStyle(ChatFormatting.AQUA))
}
