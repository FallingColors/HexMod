package at.petrak.hexcasting.common.casting.operators.spells.great

import at.petrak.hexcasting.HexMod
import at.petrak.hexcasting.api.Operator.Companion.getChecked
import at.petrak.hexcasting.api.RenderedSpell
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.api.SpellOperator
import at.petrak.hexcasting.common.casting.CastingContext
import net.minecraft.core.BlockPos
import net.minecraft.world.item.BucketItem
import net.minecraft.world.item.Items
import net.minecraft.world.phys.Vec3

object OpCreateLava : SpellOperator {
    override val argc = 1
    override val isGreat = true
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<Vec3>> {
        val target = args.getChecked<Vec3>(0)
        ctx.assertVecInRange(target)

        return Triple(
            Spell(target),
            100_000,
            listOf(target),
        )
    }

    private data class Spell(val target: Vec3) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            // Just steal bucket code lmao
            val charlie = Items.LAVA_BUCKET
            if (charlie is BucketItem) {
                // make the player null so we don't give them a usage statistic for example
                charlie.emptyContents(null, ctx.world, BlockPos(target), null)
            } else {
                HexMod.getLogger().warn("Items.LAVA_BUCKET wasn't a BucketItem?")
            }
        }
    }
}