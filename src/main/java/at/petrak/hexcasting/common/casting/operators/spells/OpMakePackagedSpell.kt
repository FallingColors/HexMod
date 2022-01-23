package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.Operator.Companion.getChecked
import at.petrak.hexcasting.api.RenderedSpell
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.api.SpellOperator
import at.petrak.hexcasting.common.casting.CastException
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.ManaHelper
import at.petrak.hexcasting.common.items.magic.ItemPackagedSpell
import at.petrak.hexcasting.hexmath.HexPattern
import net.minecraft.nbt.ListTag
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity

class OpMakePackagedSpell<T : ItemPackagedSpell>(val type: Class<T>, val cost: Int) : SpellOperator {
    override val argc = 2
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Pair<RenderedSpell, Int> {
        val otherHandItem = ctx.caster.getItemInHand(ctx.otherHand)
        if (!type.isAssignableFrom(otherHandItem.item.javaClass)) {
            throw CastException(CastException.Reason.BAD_OFFHAND_ITEM, type, otherHandItem)
        }

        val entity = args.getChecked<Entity>(0)
        val patterns = args.getChecked<List<SpellDatum<*>>>(1).map { it.tryGet<HexPattern>() }

        if (entity !is ItemEntity)
            throw CastException(CastException.Reason.OP_WRONG_TYPE, ItemEntity::class.java, entity)

        return Pair(Spell(entity, patterns), cost)
    }

    private data class Spell(val itemEntity: ItemEntity, val patterns: List<HexPattern>) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val otherHandItem = ctx.caster.getItemInHand(ctx.otherHand)
            if (otherHandItem.item is ItemPackagedSpell && !otherHandItem.hasTag() && itemEntity.isAlive) {
                val manaAmt = ManaHelper.extractAllMana(itemEntity.item)
                if (manaAmt != null) {
                    val tag = otherHandItem.orCreateTag
                    tag.putInt(ItemPackagedSpell.TAG_MANA, manaAmt)
                    tag.putInt(ItemPackagedSpell.TAG_START_MANA, manaAmt)

                    val patsTag = ListTag()
                    for (pat in patterns) {
                        patsTag.add(pat.serializeToNBT())
                    }
                    tag.put(ItemPackagedSpell.TAG_PATTERNS, patsTag)

                    if (itemEntity.item.isEmpty)
                        itemEntity.kill()
                }
            }
        }
    }
}