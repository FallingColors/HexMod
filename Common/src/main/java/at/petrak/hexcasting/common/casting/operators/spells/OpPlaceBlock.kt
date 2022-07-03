package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getBlockPos
import at.petrak.hexcasting.api.spell.iota.Iota
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

object OpPlaceBlock : SpellAction {
    override val argc: Int
        get() = 1

    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
        val pos = args.getBlockPos(0, argc)
        ctx.assertVecInRange(pos)

        if (!ctx.world.mayInteract(ctx.caster, pos))
            return null


        val blockHit = BlockHitResult(
            Vec3.ZERO, ctx.caster.direction, pos, false
        )
        val itemUseCtx = UseOnContext(ctx.caster, ctx.castingHand, blockHit)
        val placeContext = BlockPlaceContext(itemUseCtx)

        val worldState = ctx.world.getBlockState(pos)
        if (!worldState.canBeReplaced(placeContext))
            throw MishapBadBlock.of(pos, "replaceable")

        return Triple(
            Spell(pos),
            ManaConstants.DUST_UNIT / 8,
            listOf(ParticleSpray.cloud(Vec3.atCenterOf(pos), 1.0))
        )
    }

    private data class Spell(val pos: BlockPos) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            if (!ctx.world.mayInteract(ctx.caster, pos))
                return

            val blockHit = BlockHitResult(
                Vec3.ZERO, ctx.caster.direction, pos, false
            )

            val bstate = ctx.world.getBlockState(pos)
            val placeeSlot = ctx.getOperativeSlot { it.item is BlockItem }
            if (placeeSlot != null) {
                val placeeStack = ctx.caster.inventory.getItem(placeeSlot).copy()

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
                        val placee = placeeStack.item as BlockItem
                        if (ctx.withdrawItem(placee, 1, false)) {
                            val res = spoofedStack.useOn(placeContext)

                            ctx.caster.setItemInHand(ctx.castingHand, oldStack)
                            if (res != InteractionResult.FAIL) {
                                ctx.withdrawItem(placee, 1, true)

                                ctx.world.playSound(
                                    ctx.caster,
                                    pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
                                    bstate.soundType.placeSound, SoundSource.BLOCKS, 1.0f,
                                    1.0f + (Math.random() * 0.5 - 0.25).toFloat()
                                )
                                val particle = BlockParticleOption(ParticleTypes.BLOCK, bstate)
                                ctx.world.sendParticles(
                                    particle, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
                                    4, 0.1, 0.2, 0.1, 0.1
                                )
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
