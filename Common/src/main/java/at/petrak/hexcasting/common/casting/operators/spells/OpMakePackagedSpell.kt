package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getItemEntity
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadItem
import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName
import at.petrak.hexcasting.api.utils.extractMedia
import at.petrak.hexcasting.api.utils.isMediaItem
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack

// TODO: How to handle in circles
class OpMakePackagedSpell<T : ItemPackagedHex>(val itemType: T, val cost: Int) : SpellAction {
    override val argc = 2
    override fun execute(
        args: List<Iota>,
        ctx: CastingEnvironment
    ): SpellAction.Result {
        val entity = args.getItemEntity(0, argc)
        val patterns = args.getList(1, argc).toList()

        val (handStack, hand) = ctx.getHeldItemToOperateOn {
            val hexHolder = IXplatAbstractions.INSTANCE.findHexHolder(it)
            it.`is`(itemType) && hexHolder != null && !hexHolder.hasHex()
        }
            ?: throw MishapBadOffhandItem(ItemStack.EMPTY.copy(), null, itemType.description) // TODO: hack

        val hexHolder = IXplatAbstractions.INSTANCE.findHexHolder(handStack)
        if (!handStack.`is`(itemType)) {
            throw MishapBadOffhandItem(handStack, hand, itemType.description)
        } else if (hexHolder == null || hexHolder.hasHex()) {
            throw MishapBadOffhandItem.of(handStack, hand, "iota.write")
        }

        ctx.assertEntityInRange(entity)
        if (!isMediaItem(entity.item) || extractMedia(
                entity.item,
                drainForBatteries = true,
                simulate = true
            ) <= 0
        ) {
            throw MishapBadItem.of(
                entity,
                "media_for_battery"
            )
        }

        val trueName = ctx.caster?.let { MishapOthersName.getTrueNameFromArgs(patterns, it) }
        if (trueName != null)
            throw MishapOthersName(trueName)

        return SpellAction.Result(
            Spell(entity, patterns, handStack),
            cost,
            listOf(ParticleSpray.burst(entity.position(), 0.5))
        )
    }

    private inner class Spell(val itemEntity: ItemEntity, val patterns: List<Iota>, val stack: ItemStack) : RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            val hexHolder = IXplatAbstractions.INSTANCE.findHexHolder(stack)
            if (hexHolder != null
                && !hexHolder.hasHex()
                && itemEntity.isAlive
            ) {
                val entityStack = itemEntity.item.copy()
                val mediamount = extractMedia(entityStack, drainForBatteries = true)
                if (mediamount > 0) {
                    hexHolder.writeHex(patterns, mediamount)
                }

                itemEntity.item = entityStack
                if (entityStack.isEmpty)
                    itemEntity.kill()
            }
        }
    }
}
