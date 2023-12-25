package at.petrak.hexcasting.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
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
            env: CastingEnvironment
    ): SpellAction.Result {
        val target = args.getBlockPos(0, argc)
        env.assertPosInRangeForEditing(target)

        return SpellAction.Result(
            Spell(target),
            (MediaConstants.DUST_UNIT * 1.125).toLong(),
            listOf(ParticleSpray.burst(Vec3.atCenterOf(BlockPos(target)), 1.0))
        )
    }

    private data class Spell(val pos: BlockPos) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            // https://github.com/VazkiiMods/Psi/blob/master/src/main/java/vazkii/psi/common/spell/trick/PieceTrickOvergrow.java
            val hit = BlockHitResult(Vec3.ZERO, Direction.UP, pos, false)
            val fakeContext = UseOnContext(env.world, env.caster, InteractionHand.MAIN_HAND, ItemStack(Items.BONE_MEAL), hit)
            Items.BONE_MEAL.useOn(fakeContext)
        }
    }
}
