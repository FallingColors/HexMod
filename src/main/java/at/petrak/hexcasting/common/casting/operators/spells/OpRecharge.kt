package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.cap.HexCapabilities
import at.petrak.hexcasting.api.utils.ManaHelper
import at.petrak.hexcasting.api.spell.mishaps.MishapBadItem
import at.petrak.hexcasting.api.spell.mishaps.MishapBadOffhandItem
import net.minecraft.world.entity.item.ItemEntity

object OpRecharge : SpellOperator {
    override val argc = 1
    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
        val (handStack, hand) = ctx.getHeldItemToOperateOn {
            val mana = it.getCapability(HexCapabilities.MANA).resolve()
            mana.isPresent && mana.get().canRecharge() && mana.get().mana < mana.get().maxMana
        }

        val mana = handStack.getCapability(HexCapabilities.MANA).resolve()

        if (!mana.isPresent || !mana.get().canRecharge())
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

        if (mana.get().mana >= mana.get().maxMana)
            return null

        return Triple(Spell(entity), 100_000, listOf(ParticleSpray.Burst(entity.position(), 0.5)))
    }

    private data class Spell(val itemEntity: ItemEntity) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val (handStack) = ctx.getHeldItemToOperateOn {
                val mana = it.getCapability(HexCapabilities.MANA).resolve()
                mana.isPresent && mana.get().canRecharge() && mana.get().mana < mana.get().maxMana
            }
            val mana = handStack.getCapability(HexCapabilities.MANA).resolve()

            if (mana.isPresent && itemEntity.isAlive) {
                val entityStack = itemEntity.item.copy()

                val maxMana = mana.get().maxMana
                val existingMana = mana.get().mana

                val manaAmt = ManaHelper.extractMana(entityStack, maxMana - existingMana)

                mana.get().mana = manaAmt + existingMana

                itemEntity.item = entityStack
                if (entityStack.isEmpty)
                    itemEntity.kill()
            }
        }
    }
}
