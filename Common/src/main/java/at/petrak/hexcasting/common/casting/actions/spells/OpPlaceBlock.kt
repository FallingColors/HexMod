package at.petrak.hexcasting.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadBlock
import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3

object OpPlaceBlock : SpellAction {
    override val argc: Int
        get() = 1

    override fun execute(
            args: List<Iota>,
            env: CastingEnvironment
    ): SpellAction.Result {
        val pos = args.getBlockPos(0, argc)
        env.assertPosInRangeForEditing(pos)

        val blockHit = BlockHitResult(
            Vec3.atCenterOf(pos), env.caster?.direction ?: Direction.NORTH, pos, false
        )
        val itemUseCtx = env
            .getHeldItemToOperateOn { it.item is BlockItem }
            ?.stack?.let { UseOnContext(env.world, env.caster, env.castingHand, it, blockHit) }
            ?: throw MishapBadOffhandItem.of(ItemStack.EMPTY, env.castingHand, "placeable")
        val placeContext = BlockPlaceContext(itemUseCtx)

        val worldState = env.world.getBlockState(pos)
        if (!worldState.canBeReplaced(placeContext))
            throw MishapBadBlock.of(pos, "replaceable")

        return SpellAction.Result(
            Spell(pos),
            MediaConstants.DUST_UNIT / 8,
            listOf(ParticleSpray.cloud(Vec3.atCenterOf(pos), 1.0))
        )
    }

    private data class Spell(val pos: BlockPos) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val caster = env.caster

            val blockHit = BlockHitResult(
                Vec3.atCenterOf(pos), caster?.direction ?: Direction.NORTH, pos, false
            )

            val bstate = env.world.getBlockState(pos)
            val placeeStack = env.getHeldItemToOperateOn { it.item is BlockItem }?.stack
            if (placeeStack != null) {
                if (!IXplatAbstractions.INSTANCE.isPlacingAllowed(env.world, pos, placeeStack, env.caster))
                    return

                if (!placeeStack.isEmpty) {
                    // https://github.com/VazkiiMods/Psi/blob/master/src/main/java/vazkii/psi/common/spell/trick/block/PieceTrickPlaceBlock.java#L143
                    val spoofedStack = placeeStack.copy()

                    // we temporarily give the player the stack, place it using mc code, then give them the old stack back.
                    spoofedStack.count = 1

                    val itemUseCtx = UseOnContext(env.world, caster, env.castingHand, spoofedStack, blockHit)
                    val placeContext = BlockPlaceContext(itemUseCtx)
                    if (bstate.canBeReplaced(placeContext)) {
                        if (env.withdrawItem({ it == placeeStack }, 1, false)) {
                            val res = spoofedStack.useOn(placeContext)

                            if (res != InteractionResult.FAIL) {
                                env.withdrawItem({ it == placeeStack }, 1, true)

                                env.world.playSound(
                                    env.caster,
                                    pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
                                    bstate.soundType.placeSound, SoundSource.BLOCKS, 1.0f,
                                    1.0f + (Math.random() * 0.5 - 0.25).toFloat()
                                )
                                val particle = BlockParticleOption(ParticleTypes.BLOCK, bstate)
                                env.world.sendParticles(
                                    particle, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
                                    4, 0.1, 0.2, 0.1, 0.1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
