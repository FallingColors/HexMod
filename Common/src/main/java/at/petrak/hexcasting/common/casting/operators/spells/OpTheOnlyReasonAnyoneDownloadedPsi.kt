package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getBlockPos
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3


object OpTheOnlyReasonAnyoneDownloadedPsi : SpellAction {
    override val argc = 1
    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getBlockPos(0, argc)

        return Triple(
            Spell(target),
            (ManaConstants.DUST_UNIT * 1.125).toInt(),
            listOf(ParticleSpray.burst(Vec3.atCenterOf(target), 1.0))
        )
    }

    private data class Spell(val pos: BlockPos) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            // https://github.com/VazkiiMods/Psi/blob/master/src/main/java/vazkii/psi/common/spell/trick/PieceTrickOvergrow.java
            if (!ctx.world.mayInteract(ctx.caster, pos))
                return

            val hit = BlockHitResult(Vec3.ZERO, Direction.UP, pos, false)
            val save: ItemStack = ctx.caster.getItemInHand(InteractionHand.MAIN_HAND)
            ctx.caster.setItemInHand(InteractionHand.MAIN_HAND, ItemStack(Items.BONE_MEAL))
            val fakeContext = UseOnContext(ctx.caster, InteractionHand.MAIN_HAND, hit)
            ctx.caster.setItemInHand(InteractionHand.MAIN_HAND, save)
            Items.BONE_MEAL.useOn(fakeContext)
        }
    }
}
