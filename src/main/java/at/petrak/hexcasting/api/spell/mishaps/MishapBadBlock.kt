package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.misc.FrozenColorizer
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.world.item.DyeColor
import net.minecraft.world.level.Explosion
import net.minecraft.world.phys.Vec3

class MishapBadBlock(val pos: BlockPos, val expected: Component) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.LIME)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        ctx.world.explode(null, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, 2f, Explosion.BlockInteraction.NONE)
    }

    override fun particleSpray(ctx: CastingContext): ParticleSpray {
        return ParticleSpray.Burst(Vec3.atCenterOf(pos), 1.0)
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component {
        val bs = ctx.world.getBlockState(this.pos)
        return error("bad_block", expected, this.pos.toShortString(), bs.block.name)
    }

    companion object {
        @JvmStatic
        fun of(pos: BlockPos, stub: String): MishapBadBlock {
            return MishapBadBlock(pos, TranslatableComponent("hexcasting.mishap.bad_block.$stub"))
        }
    }
}
