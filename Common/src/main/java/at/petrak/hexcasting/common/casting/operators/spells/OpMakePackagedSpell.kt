package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellList
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapBadItem
import at.petrak.hexcasting.api.spell.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.spell.mishaps.MishapOthersName
import at.petrak.hexcasting.api.utils.ManaHelper
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.entity.item.ItemEntity

class OpMakePackagedSpell<T : ItemPackagedHex>(val itemType: T, val cost: Int) : SpellOperator {
    override val argc = 2
    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val entity = args.getChecked<ItemEntity>(0)
        val patterns = args.getChecked<SpellList>(1).toList()

        val (handStack, hand) = ctx.getHeldItemToOperateOn {
            val hexHolder = IXplatAbstractions.INSTANCE.findHexHolder(it)
            it.`is`(itemType) && hexHolder != null && !hexHolder.hasHex()
        }
        val hexHolder = IXplatAbstractions.INSTANCE.findHexHolder(handStack)
        if (!handStack.`is`(itemType)) {
            throw MishapBadOffhandItem(handStack, hand, itemType.description)
        } else if (hexHolder == null || hexHolder.hasHex()) {
            throw MishapBadOffhandItem.of(handStack, hand, "iota.write")
        }

        ctx.assertEntityInRange(entity)
        if (!ManaHelper.isManaItem(entity.item) || ManaHelper.extractMana(
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

        val trueName = MishapOthersName.getTrueNameFromArgs(patterns, ctx.caster)
        if (trueName != null)
            throw MishapOthersName(trueName)

        return Triple(Spell(entity, patterns), cost, listOf(ParticleSpray.Burst(entity.position(), 0.5)))
    }

    private inner class Spell(val itemEntity: ItemEntity, val patterns: List<SpellDatum<*>>) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val (handStack) = ctx.getHeldItemToOperateOn { it.`is`(itemType) }
            val hexHolder = IXplatAbstractions.INSTANCE.findHexHolder(handStack)
            if (hexHolder != null
                && !hexHolder.hasHex()
                && itemEntity.isAlive
            ) {
                val entityStack = itemEntity.item.copy()
                val manaAmt = ManaHelper.extractMana(entityStack, drainForBatteries = true)
                if (manaAmt > 0) {
                    hexHolder.writeHex(patterns, manaAmt)
                }

                itemEntity.item = entityStack
                if (entityStack.isEmpty)
                    itemEntity.kill()
            }
        }
    }
}
