package at.petrak.hexcasting.common.casting.mishaps

import at.petrak.hexcasting.HexMod
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
 * [MishapInvalidIota.reverseIdx] is the index from the *back* of the stack.
 */
class MishapInvalidIota(
    val perpetrator: SpellDatum<*>,
    val reverseIdx: Int,
    val expected: Component
) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.GRAY)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        stack[stack.size - 1 - reverseIdx] = SpellDatum.make(Widget.GARBAGE)
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component =
        error(
            "invalid_value",
            actionName(errorCtx.action),
            reverseIdx,
            expected,
            perpetrator.display()
        )

    companion object {
        @JvmStatic
        fun ofClass(perpetrator: SpellDatum<*>, reverseIdx: Int, cls: Class<*>): MishapInvalidIota {
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

                else -> {
                    HexMod.getLogger().warn("tried to call MishapInvalidData.ofClass with class $cls")
                    "unknown"
                }
            }
            return MishapInvalidIota(perpetrator, reverseIdx, TranslatableComponent(key))
        }
    }
}