package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getItemEntity
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapBadItem
import at.petrak.hexcasting.api.spell.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.utils.extractMana
import at.petrak.hexcasting.api.utils.isManaItem
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.entity.item.ItemEntity

object OpRecharge : SpellAction {
    override val argc = 1
    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
        val entity = args.getItemEntity(0, argc)

        val (handStack, hand) = ctx.getHeldItemToOperateOn {
            val mana = IXplatAbstractions.INSTANCE.findManaHolder(it)
            mana != null && mana.canRecharge() && mana.media /* doo doo da do doo */ < mana.maxMedia
        }

        val mana = IXplatAbstractions.INSTANCE.findManaHolder(handStack)

        if (mana == null || !mana.canRecharge())
            throw MishapBadOffhandItem.of(
                handStack,
                hand,
                "rechargable"
            )

        ctx.assertEntityInRange(entity)

        if (!isManaItem(entity.item)) {
            throw MishapBadItem.of(
                entity,
                "mana"
            )
        }

        if (mana.media >= mana.maxMedia)
            return null

        return Triple(
            Spell(entity),
            ManaConstants.SHARD_UNIT,
            listOf(ParticleSpray.burst(entity.position(), 0.5))
        )
    }

    private data class Spell(val itemEntity: ItemEntity) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val (handStack) = ctx.getHeldItemToOperateOn {
                val mana = IXplatAbstractions.INSTANCE.findManaHolder(it)
                mana != null && mana.canRecharge() && mana.media < mana.maxMedia
            }
            val mana = IXplatAbstractions.INSTANCE.findManaHolder(handStack)

            if (mana != null && itemEntity.isAlive) {
                val entityStack = itemEntity.item.copy()

                val maxMana = mana.maxMedia
                val existingMana = mana.media

                val manaAmt = extractMana(entityStack, maxMana - existingMana)

                mana.media = manaAmt + existingMana

                itemEntity.item = entityStack
                if (entityStack.isEmpty)
                    itemEntity.kill()
            }
        }
    }
}
