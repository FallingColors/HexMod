package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.Operator.Companion.getChecked
import at.petrak.hexcasting.api.ParticleSpray
import at.petrak.hexcasting.api.RenderedSpell
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.api.SpellOperator
import at.petrak.hexcasting.common.blocks.BlockConjured
import at.petrak.hexcasting.common.blocks.HexBlocks
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.lib.HexCapabilities
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

class OpConjure(val light: Boolean) : SpellOperator {
    override val argc = 1
    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getChecked<Vec3>(0)
        ctx.assertVecInRange(target)

        return Triple(
            Spell(target, light),
            10_000,
            listOf(ParticleSpray.Cloud(Vec3.atCenterOf(BlockPos(target)), 1.0))
        )
    }

    private data class Spell(val target: Vec3, val light: Boolean) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            if (ctx.world.getBlockState(BlockPos(target)).isAir) {
                var state = HexBlocks.CONJURED.get().defaultBlockState()
                if (this.light) {
                    state = state.setValue(BlockConjured.LIGHT, true)
                }
                ctx.world.setBlock(BlockPos(target), state, 2)

                val maybeCap = ctx.caster.getCapability(HexCapabilities.PREFERRED_COLORIZER).resolve()
                if (!maybeCap.isPresent)
                    return
                val cap = maybeCap.get()

                if (ctx.world.getBlockState(BlockPos(target)).block is BlockConjured) {
                    BlockConjured.setColor(ctx.world, BlockPos(target), cap.colorizer)
                }
            }
        }
    }
}