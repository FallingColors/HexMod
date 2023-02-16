package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.FrozenColorizer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.EnchantmentHelper

class MishapNoSpellCircle : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.LIGHT_BLUE)

    private inline fun dropAll(player: Player, stacks: MutableList<ItemStack>, filter: (ItemStack) -> Boolean = { true }) {
        for (index in stacks.indices) {
            val item = stacks[index]
            if (!item.isEmpty && filter(item)) {
                player.drop(item, true, false)
                stacks[index] = ItemStack.EMPTY
            }
        }
    }

    override fun execute(ctx: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        dropAll(ctx.caster, ctx.caster.inventory.items)
        dropAll(ctx.caster, ctx.caster.inventory.offhand)
        dropAll(ctx.caster, ctx.caster.inventory.armor) {
            !EnchantmentHelper.hasBindingCurse(it)
        }
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) =
        error("no_spell_circle")
}
