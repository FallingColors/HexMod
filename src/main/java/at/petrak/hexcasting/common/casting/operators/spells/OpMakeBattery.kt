package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.Operator.Companion.getChecked
import at.petrak.hexcasting.api.RenderedSpell
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.api.SpellOperator
import at.petrak.hexcasting.common.casting.CastException
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.ManaHelper
import at.petrak.hexcasting.common.items.HexItems
import at.petrak.hexcasting.common.items.magic.ItemManaHolder
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.phys.Vec3

object OpMakeBattery : SpellOperator {
    override val argc = 1
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<Vec3>> {
        val otherHandItem = ctx.caster.getItemInHand(ctx.otherHand)
        if (otherHandItem.item != Items.GLASS_BOTTLE) {
            throw CastException(CastException.Reason.BAD_OFFHAND_ITEM_ITEM, Items.GLASS_BOTTLE, otherHandItem)
        }
        if (otherHandItem.count != 1) {
            throw CastException(CastException.Reason.BAD_OFFHAND_COUNT, 1, otherHandItem.count)
        }

        val entity = args.getChecked<ItemEntity>(0)

        return Triple(Spell(entity), 100_000, listOf(entity.position()))
    }

    private data class Spell(val itemEntity: ItemEntity) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val otherHandItem = ctx.caster.getItemInHand(ctx.otherHand)
            if (otherHandItem.item == Items.GLASS_BOTTLE && itemEntity.isAlive) {
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