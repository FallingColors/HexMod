package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor
import net.minecraft.world.level.Explosion
import net.minecraft.world.phys.Vec3

class MishapBadBlock(val pos: BlockPos, val expected: Component) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.LIME)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        ctx.world.explode(null, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, 0.25f, Explosion.BlockInteraction.NONE)
    }

    override fun particleSpray(ctx: CastingContext) =
        ParticleSpray.burst(Vec3.atCenterOf(pos), 1.0)

    override fun errorMessage(ctx: CastingContext, errorCtx: Context) =
        error("bad_block", expected, this.pos.toShortString(), blockAtPos(ctx, this.pos))

    companion object {
        @JvmStatic
        fun of(pos: BlockPos, stub: String): MishapBadBlock {
            return MishapBadBlock(pos, "hexcasting.mishap.bad_block.$stub".asTranslatedComponent)
        }
    }
}
