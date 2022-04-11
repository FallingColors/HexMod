package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.ManaHelper
import at.petrak.hexcasting.common.casting.mishaps.MishapBadItem
import at.petrak.hexcasting.common.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.common.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.common.items.magic.ItemPackagedSpell
import at.petrak.hexcasting.hexmath.HexPattern
import net.minecraft.nbt.ListTag
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.world.entity.item.ItemEntity

class OpMakePackagedSpell<T : ItemPackagedSpell>(val itemType: T, val cost: Int) : SpellOperator {
    override val argc = 2
    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val entity = args.getChecked<ItemEntity>(0)
        val patterns = args.getChecked<List<SpellDatum<*>>>(1).map {
            if (it.payload is HexPattern)
                it.payload
            else
                throw MishapInvalidIota(it, 0, TranslatableComponent("hexcasting.mishap.invalid_value.list.pattern"))
        }

        val (handStack, hand) = ctx.getHeldItemToOperateOn { it.`is`(itemType) }
        if (!handStack.`is`(itemType)) {
            throw MishapBadOffhandItem(handStack, hand, itemType.description)
        }

        ctx.assertEntityInRange(entity)
        if (!ManaHelper.isManaItem(entity.item)) {
            throw MishapBadItem.of(
                entity.item,
                "mana"
            )
        }

        return Triple(Spell(entity, patterns), cost, listOf(ParticleSpray.Burst(entity.position(), 0.5)))
    }

    private inner class Spell(val itemEntity: ItemEntity, val patterns: List<HexPattern>) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val (handStack) = ctx.getHeldItemToOperateOn { it.`is`(itemType) }
            val tag = handStack.orCreateTag
            if (handStack.item is ItemPackagedSpell
                && !tag.contains(ItemPackagedSpell.TAG_MANA)
                && !tag.contains(ItemPackagedSpell.TAG_MAX_MANA)
                && !tag.contains(ItemPackagedSpell.TAG_PATTERNS)
                && itemEntity.isAlive
            ) {
                val entityStack = itemEntity.item.copy()
                val manaAmt = ManaHelper.extractAllMana(entityStack)
                if (manaAmt > 0) {
                    tag.putInt(ItemPackagedSpell.TAG_MANA, manaAmt)
                    tag.putInt(ItemPackagedSpell.TAG_MAX_MANA, manaAmt)

                    val patsTag = ListTag()
                    for (pat in patterns) {
                        patsTag.add(pat.serializeToNBT())
                    }
                    tag.put(ItemPackagedSpell.TAG_PATTERNS, patsTag)
                }

                itemEntity.item = entityStack
                if (entityStack.isEmpty)
                    itemEntity.kill()
            }
        }
    }
}
