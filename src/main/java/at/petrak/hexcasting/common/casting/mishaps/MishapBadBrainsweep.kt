package at.petrak.hexcasting.common.casting.mishaps

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.colors.FrozenColorizer
import at.petrak.hexcasting.common.lib.HexDamageSources
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.item.DyeColor

class MishapBadBrainsweep(val villager: Villager, val pos: BlockPos) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.LIME)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        villager.hurt(HexDamageSources.OVERCAST, villager.health)
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component {
        val bs = ctx.world.getBlockState(this.pos)
        return error("bad_brainsweep", bs.block.name)
    }
}