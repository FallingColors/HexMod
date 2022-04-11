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
import at.petrak.hexcasting.common.items.magic.ItemManaHolder
import net.minecraft.util.Mth
import net.minecraft.world.entity.item.ItemEntity

object OpRecharge : SpellOperator {
    override val argc = 1
    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val (handStack, hand) = ctx.getHeldItemToOperateOn { it.item is ItemManaHolder }

        if (handStack.item !is ItemManaHolder)
            throw MishapBadOffhandItem.of(
                handStack,
                hand,
                "rechargable"
            )

        val entity = args.getChecked<ItemEntity>(0)
        ctx.assertEntityInRange(entity)

        if (!ManaHelper.isManaItem(entity.item)) {
            throw MishapBadItem.of(
                entity.item,
                "mana"
            )
        }

        return Triple(Spell(entity), 100_000, listOf(ParticleSpray.Burst(entity.position(), 0.5)))
    }

    private data class Spell(val itemEntity: ItemEntity) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val (handStack) = ctx.getHeldItemToOperateOn { it.item is ItemManaHolder }

            if (handStack.item is ItemManaHolder && itemEntity.isAlive) {
                val entityStack = itemEntity.item.copy()

                val tag = handStack.orCreateTag
                val maxMana = if (tag.contains(ItemManaHolder.TAG_MAX_MANA))
                    tag.getInt(ItemManaHolder.TAG_MAX_MANA)
                else
                    Int.MAX_VALUE
                val existingMana = if (tag.contains(ItemManaHolder.TAG_MANA))
                    tag.getInt(ItemManaHolder.TAG_MANA)
                else
                    0

                val manaAmt = ManaHelper.extractMana(entityStack, maxMana - existingMana)

                tag.putInt(ItemManaHolder.TAG_MANA, Mth.clamp(existingMana + manaAmt, 0, maxMana))

                itemEntity.item = entityStack
                if (entityStack.isEmpty)
                    itemEntity.kill()
            }
        }
    }
}
