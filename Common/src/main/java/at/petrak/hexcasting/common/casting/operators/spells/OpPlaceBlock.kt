package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapBadBlock
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3

object OpPlaceBlock : SpellOperator {
    override val argc: Int
        get() = 1

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
        val target = args.getChecked<Vec3>(0, argc)
        ctx.assertVecInRange(target)

        val pos = BlockPos(target)

        val blockHit = BlockHitResult(
            target, ctx.caster.direction, pos, false
        )
        val itemUseCtx = UseOnContext(ctx.caster, ctx.castingHand, blockHit)
        val placeContext = BlockPlaceContext(itemUseCtx)

        val worldState = ctx.world.getBlockState(pos)
        if (!worldState.canBeReplaced(placeContext))
            throw MishapBadBlock.of(pos, "replaceable")

        return Triple(
            Spell(target),
            ManaConstants.DUST_UNIT / 8,
            listOf(ParticleSpray.cloud(Vec3.atCenterOf(pos), 1.0))
        )
    }

    private data class Spell(val vec: Vec3) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val pos = BlockPos(vec)

            if (!ctx.canEditBlockAt(pos))
                return

            val blockHit = BlockHitResult(
                vec, ctx.caster.direction, pos, false
            )

            val bstate = ctx.world.getBlockState(pos)
            val placeeStack = ctx.getOperativeSlot { it.item is BlockItem }?.copy()
            if (placeeStack != null) {
                if (!IXplatAbstractions.INSTANCE.isPlacingAllowed(ctx.world, pos, placeeStack, ctx.caster))
                    return

                if (!placeeStack.isEmpty) {
                    // https://github.com/VazkiiMods/Psi/blob/master/src/main/java/vazkii/psi/common/spell/trick/block/PieceTrickPlaceBlock.java#L143
                    val oldStack = ctx.caster.getItemInHand(ctx.castingHand)
                    val spoofedStack = placeeStack.copy()

                    // we temporarily give the player the stack, place it using mc code, then give them the old stack back.
                    spoofedStack.count = 1
                    ctx.caster.setItemInHand(ctx.castingHand, spoofedStack)

                    val itemUseCtx = UseOnContext(ctx.caster, ctx.castingHand, blockHit)
                    val placeContext = BlockPlaceContext(itemUseCtx)
                    if (bstate.canBeReplaced(placeContext)) {
                        if (ctx.withdrawItem(placeeStack, 1, false)) {
                            val res = spoofedStack.useOn(placeContext)

                            ctx.caster.setItemInHand(ctx.castingHand, oldStack)
                            if (res != InteractionResult.FAIL) {
                                ctx.withdrawItem(placeeStack, 1, true)

                                ctx.world.playSound(
                                    ctx.caster,
                                    vec.x, vec.y, vec.z, bstate.soundType.placeSound, SoundSource.BLOCKS, 1.0f,
                                    1.0f + (Math.random() * 0.5 - 0.25).toFloat()
                                )
                                val particle = BlockParticleOption(ParticleTypes.BLOCK, bstate)
                                ctx.world.sendParticles(particle, vec.x, vec.y, vec.z, 4, 0.1, 0.2, 0.1, 0.1)
                            }
                        } else {
                            ctx.caster.setItemInHand(ctx.castingHand, oldStack)
                        }
                    } else {
                        ctx.caster.setItemInHand(ctx.castingHand, oldStack)
                    }
                }
            }
        }
    }
}
