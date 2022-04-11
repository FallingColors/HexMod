package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.ManaHelper
import at.petrak.hexcasting.common.casting.colors.FrozenColorizer
import at.petrak.hexcasting.common.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.common.items.HexItems
import at.petrak.hexcasting.common.items.magic.ItemManaHolder
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object OpMakeBattery : SpellOperator {
    override val argc = 1
    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val entity = args.getChecked<ItemEntity>(0)

        val otherHandItem = ctx.getHeldItemToOperateOn { it.item == Items.GLASS_BOTTLE }

        if (otherHandItem.item != Items.GLASS_BOTTLE) {
            throw MishapBadOffhandItem.of(
                otherHandItem,
                "bottle"
            )
        }
        if (otherHandItem.count != 1) {
            throw MishapBadOffhandItem.of(
                otherHandItem,
                "only_one"
            )
        }

        ctx.assertEntityInRange(entity)

        if (!ManaHelper.isManaItem(entity.item)) {
            throw MishapBadOffhandItem.of(
                otherHandItem,
                "mana"
            )
        }

        return Triple(Spell(entity), 100_000, listOf(ParticleSpray.Burst(entity.position(), 0.5)))
    }

    private data class Spell(val itemEntity: ItemEntity) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val handStack = ctx.getHeldItemToOperateOn { it.item == Items.GLASS_BOTTLE }
            if (handStack.item == Items.GLASS_BOTTLE && itemEntity.isAlive) {
                val manaAmt = ManaHelper.extractAllMana(itemEntity.item)
                if (manaAmt != null) {
                    val replaceItem = ItemStack(HexItems.BATTERY.get())
                    val tag = replaceItem.orCreateTag
                    tag.putInt(ItemManaHolder.TAG_MANA, manaAmt)
                    tag.putInt(ItemManaHolder.TAG_MAX_MANA, manaAmt)

                    ctx.caster.setItemInHand(ctx.otherHand, replaceItem)
                }
            }
        }
    }
}
