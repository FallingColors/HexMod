package at.petrak.hexcasting.api.casting.mishaps.circle

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments

class MishapNoSpellCircle : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.LIGHT_BLUE)

    // FIXME: make me work with any entity and not just players
    private inline fun dropAll(player: Player, stacks: MutableList<ItemStack>, filter: (ItemStack) -> Boolean = { true }) {
        for (index in stacks.indices) {
            val item = stacks[index]
            if (!item.isEmpty && filter(item)) {
                player.drop(item, true, false)
                stacks[index] = ItemStack.EMPTY
            }
        }
    }

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        val caster = env.castingEntity as? ServerPlayer
        if (caster != null) {
            // FIXME: handle null caster case
            dropAll(caster, caster.inventory.items)
            dropAll(caster, caster.inventory.offhand)
            dropAll(caster, caster.inventory.armor) {
                it.get(DataComponents.ENCHANTMENTS)?.keySet()?.any { e -> e.`is`(Enchantments.BINDING_CURSE) } != true
            }
        }
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) =
        error("no_spell_circle")
}
