package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.mod.HexItemTags
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapBadItem
import at.petrak.hexcasting.api.spell.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.utils.extractMana
import at.petrak.hexcasting.api.utils.isManaItem
import at.petrak.hexcasting.common.items.magic.ItemManaHolder
import at.petrak.hexcasting.common.lib.HexItems
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack

object OpMakeBattery : SpellOperator {
    override val argc = 1

    override val isGreat = true

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val entity = args.getChecked<ItemEntity>(0, argc)

        val (handStack, hand) = ctx.getHeldItemToOperateOn { it.`is`(HexItemTags.PHIAL_BASE) }

        if (!handStack.`is`(HexItemTags.PHIAL_BASE)) {
            throw MishapBadOffhandItem.of(
                handStack,
                hand,
                "bottle"
            )
        }
        if (handStack.count != 1) {
            throw MishapBadOffhandItem.of(
                handStack,
                hand,
                "only_one"
            )
        }

        ctx.assertEntityInRange(entity)

        if (!isManaItem(entity.item) || extractMana(
                entity.item,
                drainForBatteries = true,
                simulate = true
            ) <= 0
        ) {
            throw MishapBadItem.of(
                entity,
                "mana_for_battery"
            )
        }

        return Triple(Spell(entity, hand),
            ManaConstants.CRYSTAL_UNIT, listOf(ParticleSpray.burst(entity.position(), 0.5)))
    }

    private data class Spell(val itemEntity: ItemEntity, val hand: InteractionHand) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            if (itemEntity.isAlive) {
                val entityStack = itemEntity.item.copy()
                val manaAmt = extractMana(entityStack, drainForBatteries = true)
                if (manaAmt > 0) {
                    ctx.caster.setItemInHand(
                        hand,
                        ItemManaHolder.withMana(ItemStack(HexItems.BATTERY), manaAmt, manaAmt)
                    )
                }

                itemEntity.item = entityStack
                if (entityStack.isEmpty)
                    itemEntity.kill()
            }
        }
    }
}
