package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.ParticleSpray
import at.petrak.hexcasting.api.RenderedSpell
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.api.SpellOperator
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.items.HexItems
import at.petrak.hexcasting.common.items.ItemDataHolder
import at.petrak.hexcasting.common.items.ItemScroll
import at.petrak.hexcasting.hexmath.HexPattern
import net.minecraft.world.item.ItemStack

// we make this a spell cause imo it's a little ... anticlimactic for it to just make no noise
object OpWrite : SpellOperator {
    override val argc = 1
    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        return Triple(
            Spell(args[0]),
            0,
            listOf()
        )
    }

    private data class Spell(val datum: SpellDatum<*>) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val handStack = ctx.caster.getItemInHand(ctx.otherHand)
            val handItem = handStack.item
            val tag = handStack.orCreateTag
            if (handItem is ItemDataHolder) {
                handItem.writeDatum(tag, datum)
            } else if (handItem == HexItems.SCROLL.get() && !tag.contains(ItemScroll.TAG_PATTERN) && datum.payload is HexPattern) {
                tag.put(ItemScroll.TAG_PATTERN, datum.payload.serializeToNBT())
            } else if (handItem == HexItems.BLANK_SLATE.get() && datum.payload is HexPattern) {
                val newStack = ItemStack(HexItems.WRITTEN_SLATE.get(), handStack.count)
                val newTag = tag.copy() // in case you named it or something??
                newTag.put(BlockEntitySlate.TAG_PATTERN, datum.payload.serializeToNBT())
                newStack.tag = newTag
                ctx.caster.setItemInHand(ctx.otherHand, newStack)
            } else {
                // Fuck
            }
        }

    }
}