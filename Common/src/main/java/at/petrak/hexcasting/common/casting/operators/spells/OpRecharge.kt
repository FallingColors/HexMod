package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapBadItem
import at.petrak.hexcasting.api.spell.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.utils.extractMana
import at.petrak.hexcasting.api.utils.isManaItem
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack

object OpRecharge : SpellOperator {
    override val argc = 1
    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
        val (handStack, hand) = ctx.getHeldItemToOperateOn {
            val mana = IXplatAbstractions.INSTANCE.findManaHolder(it)
            mana != null && mana.canRecharge() && mana.insertMana(-1, true) != 0
        }

        val mana = IXplatAbstractions.INSTANCE.findManaHolder(handStack)

        if (mana == null || !mana.canRecharge())
            throw MishapBadOffhandItem.of(
                handStack,
                hand,
                "rechargable"
            )

        val entity = args.getChecked<ItemEntity>(0, argc)
        ctx.assertEntityInRange(entity)

        if (!isManaItem(entity.item)) {
            throw MishapBadItem.of(
                entity,
                "mana"
            )
        }

        if (mana.insertMana(-1, true) == 0)
            return null

        return Triple(
            Spell(entity, handStack),
            ManaConstants.SHARD_UNIT,
            listOf(ParticleSpray.burst(entity.position(), 0.5))
        )
    }

    private data class Spell(val itemEntity: ItemEntity, val stack: ItemStack) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val mana = IXplatAbstractions.INSTANCE.findManaHolder(stack)

            if (mana != null && itemEntity.isAlive) {
                val entityStack = itemEntity.item.copy()

                val emptySpace = mana.insertMana(-1, true)

                val manaAmt = extractMana(entityStack, emptySpace)

                mana.insertMana(manaAmt, false)

                itemEntity.item = entityStack
                if (entityStack.isEmpty)
                    itemEntity.kill()
            }
        }
    }
}
