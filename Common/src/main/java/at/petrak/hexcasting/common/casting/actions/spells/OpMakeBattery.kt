package at.petrak.hexcasting.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getItemEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadItem
import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.mod.HexTags
import at.petrak.hexcasting.api.utils.extractMedia
import at.petrak.hexcasting.api.utils.isMediaItem
import at.petrak.hexcasting.common.items.magic.ItemMediaHolder
import at.petrak.hexcasting.common.lib.HexItems
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack

// TODO: how to handle in cirles
object OpMakeBattery : SpellAction {
    override val argc = 1

    override fun execute(
            args: List<Iota>,
            env: CastingEnvironment
    ): SpellAction.Result {
        val entity = args.getItemEntity(0, argc)

        val (handStack, hand) = env.getHeldItemToOperateOn { it.`is`(HexTags.Items.PHIAL_BASE) }
            ?: throw MishapBadOffhandItem.of(ItemStack.EMPTY.copy(), "bottle") // TODO: hack

        if (!handStack.`is`(HexTags.Items.PHIAL_BASE)) {
            throw MishapBadOffhandItem.of(
                handStack,
                "bottle"
            )
        }
        if (handStack.count != 1) {
            throw MishapBadOffhandItem.of(
                handStack,
                "only_one"
            )
        }

        env.assertEntityInRange(entity)

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

        return SpellAction.Result(
            Spell(entity, handStack, hand),
            MediaConstants.CRYSTAL_UNIT,
            listOf(ParticleSpray.burst(entity.position(), 0.5))
        )
    }

    private data class Spell(val itemEntity: ItemEntity, val handStack: ItemStack, val hand: InteractionHand?) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            if (!itemEntity.isAlive)
                return

            val entityStack = itemEntity.item.copy()
            val mediamount = extractMedia(entityStack, drainForBatteries = true)
            if (mediamount > 0) {
                if (!env.replaceItem({ it == handStack }, ItemMediaHolder.withMedia(ItemStack(HexItems.BATTERY), mediamount, mediamount), hand))
                    return
            }

            itemEntity.item = entityStack
            if (entityStack.isEmpty)
                itemEntity.kill()
        }
    }
}
