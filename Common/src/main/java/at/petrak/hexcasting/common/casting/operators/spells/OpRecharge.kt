package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapBadItem
import at.petrak.hexcasting.api.spell.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.utils.ManaHelper
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.entity.item.ItemEntity

object OpRecharge : SpellOperator {
    override val argc = 1
    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
        val (handStack, hand) = ctx.getHeldItemToOperateOn {
            val mana = IXplatAbstractions.INSTANCE.findManaHolder(it)
            mana != null && mana.canRecharge() && mana.mana /* doo doo da do doo */ < mana.maxMana
        }

        val mana = IXplatAbstractions.INSTANCE.findManaHolder(handStack)

        if (mana == null || !mana.canRecharge())
            throw MishapBadOffhandItem.of(
                handStack,
                hand,
                "rechargable"
            )

        val entity = args.getChecked<ItemEntity>(0)
        ctx.assertEntityInRange(entity)

        if (!ManaHelper.isManaItem(entity.item)) {
            throw MishapBadItem.of(
                entity,
                "mana"
            )
        }

        if (mana.mana >= mana.maxMana)
            return null

        return Triple(Spell(entity), 100_000, listOf(ParticleSpray.Burst(entity.position(), 0.5)))
    }

    private data class Spell(val itemEntity: ItemEntity) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val (handStack) = ctx.getHeldItemToOperateOn {
                val mana = IXplatAbstractions.INSTANCE.findManaHolder(it)
                mana != null && mana.canRecharge() && mana.mana < mana.maxMana
            }
            val mana = IXplatAbstractions.INSTANCE.findManaHolder(handStack)

            if (mana != null && itemEntity.isAlive) {
                val entityStack = itemEntity.item.copy()

                val maxMana = mana.maxMana
                val existingMana = mana.mana

                val manaAmt = ManaHelper.extractMana(entityStack, maxMana - existingMana)

                mana.mana = manaAmt + existingMana

                itemEntity.item = entityStack
                if (entityStack.isEmpty)
                    itemEntity.kill()
            }
        }
    }
}
