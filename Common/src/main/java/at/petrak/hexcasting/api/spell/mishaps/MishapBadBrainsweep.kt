package at.petrak.hexcasting.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.misc.HexDamageSources
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.item.DyeColor
import net.minecraft.world.phys.Vec3

class MishapBadBrainsweep(val villager: Villager, val pos: BlockPos) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
        dyeColor(DyeColor.GREEN)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        trulyHurt(villager, HexDamageSources.overcastDamageFrom(ctx.caster), villager.health)
    }

    override fun particleSpray(ctx: CastingContext): ParticleSpray {
        return ParticleSpray.Burst(Vec3.atCenterOf(pos), 1.0)
    }

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component {
        val bs = ctx.world.getBlockState(this.pos)
        return error("bad_brainsweep", bs.block.name)
    }
}
