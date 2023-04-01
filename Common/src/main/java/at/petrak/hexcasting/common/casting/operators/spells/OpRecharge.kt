package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getItemEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadItem
import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.utils.extractMedia
import at.petrak.hexcasting.api.utils.isMediaItem
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack

object OpRecharge : SpellAction {
    override val argc = 1
    override fun execute(
        args: List<Iota>,
        ctx: CastingEnvironment
    ): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
        val entity = args.getItemEntity(0, argc)

        val (handStack, hand) = ctx.getHeldItemToOperateOn {
            val media = IXplatAbstractions.INSTANCE.findMediaHolder(it)
            media != null && media.canRecharge() && media.insertMedia(-1, true) != 0L
        }

        val media = IXplatAbstractions.INSTANCE.findMediaHolder(handStack)

        if (media == null || !media.canRecharge())
            throw MishapBadOffhandItem.of(
                handStack,
                hand,
                "rechargable"
            )

        ctx.assertEntityInRange(entity)

        if (!isMediaItem(entity.item)) {
            throw MishapBadItem.of(
                entity,
                "media"
            )
        }

        if (media.insertMedia(-1, true) == 0L)
            return null

        return Triple(
            Spell(entity, handStack),
            MediaConstants.SHARD_UNIT,
            listOf(ParticleSpray.burst(entity.position(), 0.5))
        )
    }

    private data class Spell(val itemEntity: ItemEntity, val stack: ItemStack) : RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            val media = IXplatAbstractions.INSTANCE.findMediaHolder(stack)

            if (media != null && itemEntity.isAlive) {
                val entityStack = itemEntity.item.copy()

                val emptySpace = media.insertMedia(-1, true)

                val mediaAmt = extractMedia(entityStack, emptySpace)

                media.insertMedia(mediaAmt, false)

                itemEntity.item = entityStack
                if (entityStack.isEmpty)
                    itemEntity.kill()
            }
        }
    }
}
