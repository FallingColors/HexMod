package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.Operator.Companion.getChecked
import at.petrak.hexcasting.api.RenderedSpell
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.api.SpellOperator
import at.petrak.hexcasting.common.casting.CastException
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.ManaHelper
import at.petrak.hexcasting.common.items.magic.ItemManaHolder
import net.minecraft.util.Mth
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.phys.Vec3

object OpRecharge : SpellOperator {
    override val argc = 1
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<Vec3>> {
        val otherHandItem = ctx.caster.getItemInHand(ctx.otherHand)
        if (otherHandItem.item !is ItemManaHolder) {
            throw CastException(CastException.Reason.BAD_OFFHAND_ITEM, ItemManaHolder::class.java, otherHandItem)
        }

        val entity = args.getChecked<ItemEntity>(0)

        return Triple(Spell(entity), 100_000, listOf(entity.position()))
    }

    private data class Spell(val itemEntity: ItemEntity) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val otherHandItem = ctx.caster.getItemInHand(ctx.otherHand)
            if (otherHandItem.item is ItemManaHolder && itemEntity.isAlive) {
                val manaAmt = ManaHelper.extractAllMana(itemEntity.item)
                if (manaAmt != null) {
                    val tag = otherHandItem.orCreateTag
                    val maxMana = if (tag.contains(ItemManaHolder.TAG_MAX_MANA))
                        tag.getInt(ItemManaHolder.TAG_MAX_MANA)
                    else
                        Int.MAX_VALUE
                    val existingMana = if (tag.contains(ItemManaHolder.TAG_MANA))
                        tag.getInt(ItemManaHolder.TAG_MANA)
                    else
                        0
                    tag.putInt(ItemManaHolder.TAG_MANA, Mth.clamp(existingMana + manaAmt, 0, maxMana))
                }
            }
        }
    }
}