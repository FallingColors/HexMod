package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.item.ManaHolder
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
import net.minecraft.world.entity.item.ItemEntity

object OpRecharge : SpellOperator {
    override val argc = 1
    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
        val (handStack, hand) = ctx.getHeldItemToOperateOn {
            val item = it.item
            item is ManaHolder && item.getMana(it) < item.getMaxMana(it)
        }

        val handItem = handStack.item

        if (handItem !is ManaHolder)
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

        if (handItem.getMana(handStack) >= handItem.getMana(handStack))
            return null

        return Triple(Spell(entity), 100_000, listOf(ParticleSpray.Burst(entity.position(), 0.5)))
    }

    private data class Spell(val itemEntity: ItemEntity) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val (handStack) = ctx.getHeldItemToOperateOn {
                val item = it.item
                item is ManaHolder && item.getMana(it) < item.getMaxMana(it)
            }
            val handItem = handStack.item

            if (handItem is ManaHolder && itemEntity.isAlive) {
                val entityStack = itemEntity.item.copy()

                val maxMana = handItem.getMaxMana(handStack)
                val existingMana = handItem.getMana(handStack)

                val manaAmt = ManaHelper.extractMana(entityStack, maxMana - existingMana)

                handItem.setMana(handStack, manaAmt + existingMana)

                itemEntity.item = entityStack
                if (entityStack.isEmpty)
                    itemEntity.kill()
            }
        }
    }
}
