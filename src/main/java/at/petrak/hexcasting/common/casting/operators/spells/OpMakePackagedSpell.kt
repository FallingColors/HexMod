package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.cap.HexCapabilities
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
import at.petrak.hexcasting.common.items.magic.ItemPackagedSpell
import net.minecraft.world.entity.item.ItemEntity

class OpMakePackagedSpell<T : ItemPackagedSpell>(val itemType: T, val cost: Int) : SpellOperator {
    override val argc = 2
    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val entity = args.getChecked<ItemEntity>(0)
        val patterns = args.getChecked<SpellList>(1).toList()

        val (handStack, hand) = ctx.getHeldItemToOperateOn {
            val spellHolder = HexCapabilities.getCapability(it, HexCapabilities.SPELL)
            it.`is`(itemType) && spellHolder.isPresent && !spellHolder.get().hasSpell()
        }
        val spellHolder = HexCapabilities.getCapability(handStack, HexCapabilities.SPELL)
        if (!handStack.`is`(itemType)) {
            throw MishapBadOffhandItem(handStack, hand, itemType.description)
        } else if (spellHolder.get().hasSpell()) {
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
            val spellHolder = HexCapabilities.getCapability(handStack, HexCapabilities.SPELL)
            if (spellHolder.isPresent
                && !spellHolder.get().hasSpell()
                && itemEntity.isAlive
            ) {
                val entityStack = itemEntity.item.copy()
                val manaAmt = ManaHelper.extractMana(entityStack, drainForBatteries = true)
                if (manaAmt > 0) {
                    spellHolder.get().writePatterns(patterns, manaAmt)
                }

                itemEntity.item = entityStack
                if (entityStack.isEmpty)
                    itemEntity.kill()
            }
        }
    }
}
