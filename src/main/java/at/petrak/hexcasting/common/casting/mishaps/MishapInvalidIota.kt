package at.petrak.hexcasting.common.casting.mishaps

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.Widget
import at.petrak.hexcasting.common.casting.colors.FrozenColorizer
import at.petrak.hexcasting.hexmath.HexPattern
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.DyeColor
import net.minecraft.world.phys.Vec3

/**
 * The value failed some kind of predicate.
 *
 * [MishapInvalidIota.idx] is the absolute index in the stack.
 */
class MishapInvalidIota(
    val perpetrator: SpellDatum<*>,
    val idx: Int,
    val expectedKey: String
) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.GRAY)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        stack[idx] = SpellDatum.make(Widget.GARBAGE)
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component =
        error(
            "invalid_value",
            actionName(errorCtx.action!!),
            idx,
            TranslatableComponent(expectedKey),
            perpetrator.display()
        )

    companion object {
        @JvmStatic
        fun ofClass(perpetrator: SpellDatum<*>, idx: Int, cls: Class<*>): MishapInvalidIota {
            val key = "hexcasting.mishap.invalid_value.class." + when {
                Double::class.java.isAssignableFrom(cls) -> "double"
                Vec3::class.java.isAssignableFrom(cls) -> "vector"
                List::class.java.isAssignableFrom(cls) -> "list"
                Widget::class.java.isAssignableFrom(cls) -> "widget"
                HexPattern::class.java.isAssignableFrom(cls) -> "pattern"

                ItemEntity::class.java.isAssignableFrom(cls) -> "entity.item"
                Player::class.java.isAssignableFrom(cls) -> "entity.player"
                Villager::class.java.isAssignableFrom(cls) -> "entity.player"
                LivingEntity::class.java.isAssignableFrom(cls) -> "entity.living"
                Entity::class.java.isAssignableFrom(cls) -> "entity"

                else -> "unknown"
            }
            return MishapInvalidIota(perpetrator, idx, key)
        }
    }
}